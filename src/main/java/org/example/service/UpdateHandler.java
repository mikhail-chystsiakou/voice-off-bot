package org.example.service;

import org.example.Constants;
import org.example.config.BotConfig;
import org.example.storage.VoiceStorage;
import org.example.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.GetUserProfilePhotos;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.UserProfilePhotos;
import org.telegram.telegrambots.meta.api.objects.Voice;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class UpdateHandler {
    private static final Logger logger = LoggerFactory.getLogger(UpdateHandler.class);

    @Autowired
    UserService userService;

    @Autowired
    @Lazy
    ExecuteFunction executeFunction;

    @Autowired
    @Lazy
    SendAudioFunction sendAudioFunction;

    @Autowired
    VoiceStorage voiceStorage;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    BotConfig botConfig;

    @Autowired
    FileUtils fileUtils;

    @Autowired
    StatsService statsService;

    @Autowired
    PullProcessingSet pullProcessingSet;

    public void handleVoiceMessage(Message message) throws TelegramApiException {

        Voice voice = message.getVoice();
        voiceStorage.storeVoice(
                message.getFrom().getId(),
                voice.getFileId(),
                voice.getDuration(),
                executeFunction::execute,
                message.getMessageId()
        );

        SendMessage reply = new SendMessage();
        reply.setText("Ok, recorded");
        reply.setChatId(message.getChatId());
        reply.setReplyMarkup(ButtonsService.getButtonForDeletingRecord(message.getMessageId()));
        executeFunction.execute(reply);
    }

    public void handleContact(Message message) throws TelegramApiException
    {
        long userId = message.getFrom().getId();
        long contactId = message.getUserShared().getUserId();
        long chatId = message.getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(ButtonsService.getInitMenuButtons());

        if (userService.getUserById(contactId) == null)
        {
            sendMessage.setText("Contact not found");
            executeFunction.execute(sendMessage);
            return;
        }
        if (Integer.valueOf(1).equals(userService.getSubscriberByUserIdAndSubscriberId(userId, contactId)))
        {
            sendMessage.setText("Contact is already linked to user");
            executeFunction.execute(sendMessage);
            return;
        }
        Long foloweeChatId = userService.getChatIdByUserId(contactId);
        SendMessage messageForFolowee = new SendMessage();
        messageForFolowee.setChatId(foloweeChatId);
        messageForFolowee.setText("Hi! " + getUserNameWithAt(message) + " send request to follow you. Do you confirm?");
        messageForFolowee.setReplyMarkup(ButtonsService.getInlineKeyboardMarkupForSubscription());

        sendMessage.setText("Your request was sent");

        if (!Integer.valueOf(1).equals(userService.getRequestRecord(userId, contactId))){
            int result = userService.addRequestToConfirm(userId, contactId);
        }

        executeFunction.execute(sendMessage);
        executeFunction.execute(messageForFolowee);
    }

    public void handleConfirmation(CallbackQuery callbackQuery) throws TelegramApiException
    {
        String[] data = callbackQuery.getData().split("_");
        logger.debug(Arrays.toString(data));
        System.out.println(callbackQuery.getData());
        System.out.println(Arrays.toString(data));

        String answer = data[0];
        Long userId = Long.valueOf(data[1]);

        SendMessage messageToFollowee = new SendMessage();
        SendMessage messageToUser = new SendMessage();

        messageToFollowee.setChatId(callbackQuery.getMessage().getChatId());
        Long followeeId = callbackQuery.getFrom().getId();

        if (Constants.YES.equals(answer))
        {
            userService.addContact(userId, followeeId);
            messageToFollowee.setText("User was accepted");
            messageToUser.setChatId(userService.getChatIdByUserId(userId));
            messageToUser.setText("User " + getUserNameWithAt(callbackQuery) + " accepted you");
        }

        if (Constants.No.equals(answer))
        {
            messageToFollowee.setText("Ok, subscribe request declined");
            messageToUser.setChatId(userService.getChatIdByUserId(userId));
            messageToUser.setText("User " + getUserNameWithAt(callbackQuery) + " declined you");
        }
        executeFunction.execute(messageToFollowee);
        executeFunction.execute(messageToUser);
    }

    public void registerUser(Message message) throws TelegramApiException, IOException
    {
        User user = message.getFrom();
        int result = userService.addUser(
                user.getId(),
                message.getChatId(),
                user.getUserName(),
                user.getFirstName(),
                user.getLastName()
        );
        logger.info("User registered: {}", user);

        redownloadUserPhoto(user.getId());


        String replyMessage = result == 1 ? Constants.YOU_WAS_ADDED_TO_THE_SYSTEM : Constants.YOU_HAVE_ALREADY_REGISTERED;
        SendMessage sendMessage = new SendMessage(message.getChatId().toString(), replyMessage);
        sendMessage.setReplyMarkup(ButtonsService.getInitMenuButtons());
        executeFunction.execute(sendMessage);
    }

    private void redownloadUserPhoto(Long userId) throws TelegramApiException {
        logger.trace("Trying to re-download profile photo of user with id '{}'", userId);
        GetUserProfilePhotos guph = new GetUserProfilePhotos();

        guph.setUserId(userId);
        guph.setOffset(0);
        guph.setLimit(1);
        UserProfilePhotos photos = executeFunction.execute(guph);

        if (photos.getPhotos().isEmpty()) {
            logger.trace("No photos available for user '{}'", userId);
            return;
        }
        List<PhotoSize> photoSizes = photos.getPhotos().get(0);
        if (photoSizes.isEmpty()) {
            logger.trace("No photo sizes available for user '{}'", userId);
            return;
        }
        PhotoSize photo = photoSizes.get(0);
        if (photo == null) {
            logger.trace("No photo size available for user '{}'", userId);
            return;
        }

        if (!isUserPhotoExists(userId, photo.getFileId())){
            removePreviousImages(userId);
            saveImage(photo, userId);
        }
    }

    private void removePreviousImages(Long userId)
    {
        logger.trace("Removing old images for user '{}'", userId);
        String folderPath = botConfig.getStoragePath() + botConfig.getProfilePicturesPath();
        File folder = new File(folderPath);
        final File[] files = folder.listFiles((dir,name) -> name.matches(userId.toString() + ".*?"));
        if (files == null) {
            logger.trace("No old images for user '{}'", userId);
            return;
        }
        Arrays.stream(files).forEach((f) -> {
            boolean result = f.delete();
            logger.debug("Removing old image of user {}, {}: {}", userId, f.getAbsolutePath(), result);
        });
    }

    private boolean isUserPhotoExists(Long userId, String photoId)
    {
        String filePath = fileUtils.getProfilePicturePath(userId, photoId);
        File file = new File(filePath);
        return file.exists();
    }

    private void saveImage(PhotoSize photoSize, long userId) throws TelegramApiException
    {
        GetFile getFileCommand = new GetFile();
        getFileCommand.setFileId(photoSize.getFileId());
        org.telegram.telegrambots.meta.api.objects.File downloadedFile
                    = executeFunction.execute(getFileCommand);
        String sourceFilename = downloadedFile.getFilePath();

        String destFilename = fileUtils.getProfilePicturePath(userId, photoSize.getFileId());
        logger.debug("Copying profile picture from {} to {}", sourceFilename, destFilename);
        fileUtils.moveFileAbsolute(sourceFilename, destFilename);
    }

    private String getUserName(Message message)
    {
        if (message.getFrom().getUserName() != null)
        {
            return message.getFrom().getUserName();
        }
        if (message.getFrom().getLastName() != null)
        {
            return message.getFrom().getFirstName() + " " + message.getFrom().getLastName();
        }
        return message.getFrom().getFirstName();
    }

    private String getUserNameWithAt(Message message)
    {
        if (message.getFrom().getUserName() != null)
        {
            return "@" + message.getFrom().getUserName();
        }
        if (message.getFrom().getLastName() != null)
        {
            return message.getFrom().getFirstName() + " " + message.getFrom().getLastName();
        }
        return message.getFrom().getFirstName();
    }

    private String getUserNameWithAt(CallbackQuery message)
    {
        if (message.getFrom().getUserName() != null)
        {
            return "@" + message.getFrom().getUserName();
        }
        if (message.getFrom().getLastName() != null)
        {
            return message.getFrom().getFirstName() + " " + message.getFrom().getLastName();
        }
        return message.getFrom().getFirstName();
    }

    public void pull(Message message) throws TelegramApiException, IOException
    {
        long userId = message.getFrom().getId();
        if (!pullProcessingSet.getAndSetPullProcessingForUser(userId)) {
            executeFunction.execute(new SendMessage(message.getChatId().toString(), "Preparing audios, please wait"));
            return;
        }
        statsService.init();
        statsService.pullStart();
        statsService.setUserId(userId);
        redownloadUserPhoto(userId);
        changeUserName(message.getFrom());
        if (!userService.isDataAvailable(userId))
        {
            executeFunction.execute(new SendMessage(message.getChatId().toString(), "No updates"));
        }else {
            executeFunction.execute(new SendMessage(message.getChatId().toString(), "Preparing audios for you..."));
            List<SendAudio> records = userService.pullAllRecordsForUser(userId, message.getChatId());
            records.forEach(record -> {
                try
                {
                    sendAudioFunction.execute(record);
                    userService.cleanup(record);
                }
                catch (TelegramApiException e)
                {
                    e.printStackTrace();
                }
            });
            if (userService.isDataAvailable(message.getFrom().getId())) {
                executeFunction.execute(new SendMessage(message.getChatId().toString(), "More audios available..."));
            }
        }
        statsService.pullEnd();
        statsService.storePullStatistics();
        pullProcessingSet.finishProcessingForUser(userId);
    }

    private void changeUserName(User user)
    {
        Map<String, String> existingUserNames = userService.getUserNamesByUserId(user.getId());

        if (existingUserNames != null)
        {
            String existingUserName = existingUserNames.get("username");
            String existingUserFirstName = existingUserNames.get("first_name");
            String existingLastName = existingUserNames.get("last_name");

            if (user.getUserName() != null && (existingUserName == null || !user.getUserName().equals(existingUserName))){
                userService.updateNameColumn(user.getId(), "username", user.getUserName());
            }

            if (user.getUserName() == null && existingUserName != null){
                userService.updateNameColumn(user.getId(), "username", null);
            }

            if (!user.getFirstName().equals(existingUserFirstName)){
                userService.updateNameColumn(user.getId(), "first_name", user.getFirstName());
            }

            if (user.getLastName() != null && (existingLastName == null || !user.getLastName().equals(existingLastName))){
                userService.updateNameColumn(user.getId(), "last_name", user.getLastName());
            }

            if (user.getLastName() == null && existingLastName != null){
                userService.updateNameColumn(user.getId(), "last_name", null);
            }
        }
    }

    public void getFollowers(Message message) throws TelegramApiException
    {
        executeFunction.execute(userService.getFollowers(message.getFrom().getId(), message.getChatId()));
    }

    public void getSubscriptions(Message message) throws TelegramApiException
    {
        executeFunction.execute(userService.getSubscriptions(message.getFrom().getId(), message.getChatId()));
    }

    public void unsubscribe(Message message) throws TelegramApiException
    {
        Long followeeId = message.getUserShared().getUserId();

        SendMessage result = new SendMessage();
        result.setChatId(message.getChatId());

        UserService.UserInfo followee = userService.loadUserInfoById(followeeId);
        if (followee == null)
        {
            result.setText("Selected user doesn't use the bot");
        }
        else
        {
            int updatedRows = userService.unsubscribe(message, followee);
            if (updatedRows > 0) {
                result.setText("Ok, unsubscribed");

                SendMessage unsubscribeNotification = new SendMessage();
                unsubscribeNotification.setChatId(followee.getChatId());
                unsubscribeNotification.setText(getUserNameWithAt(message) + " unsubscribed");
                executeFunction.execute(unsubscribeNotification);
            } else {
                result.setText("Nothing changed, are you really subscribed to selected user?");
            }
        }
        result.setReplyMarkup(ButtonsService.getInitMenuButtons());
        executeFunction.execute(result);
    }

    public void removeFollower(Message message) throws TelegramApiException
    {
        Long userId = message.getUserShared().getUserId();
        SendMessage result = new SendMessage();
        result.setChatId(message.getChatId());
        UserService.UserInfo follower = userService.loadUserInfoById(userId);
        if (follower == null)
        {
            result.setText("Selected user doesn't use the bot");
        }
        else
        {
            int updatedRows = userService.removeFollower(message, follower);
            if (updatedRows > 0) {
                result.setText("Ok, user will no longer receive your updates");

                SendMessage unsubscribeNotification = new SendMessage();
                unsubscribeNotification.setChatId(follower.getChatId());
                unsubscribeNotification.setText(getUserNameWithAt(message) + " revoked your subscription");
                executeFunction.execute(unsubscribeNotification);
            } else {
                result.setText("Nothing changed, are you really subscribed to selected user?");
            }
        }
        result.setReplyMarkup(ButtonsService.getInitMenuButtons());
        executeFunction.execute(result);
    }

    public void end(Message message) throws TelegramApiException
    {
        executeFunction.execute(userService.removeUser(message.getFrom().getId(), message.getChatId()));
    }

    public void help(Message message) throws TelegramApiException
    {
        executeFunction.execute(new SendMessage(message.getChatId().toString(), "This is help"));
    }

    public void unsupportedResponse(Message message) throws TelegramApiException
    {
        executeFunction.execute(new SendMessage(message.getChatId().toString(), "Only voice messages will be recorded"));
    }

    public void userNotRegistered(Message message) throws TelegramApiException
    {
        logger.trace("Sending not-registered warning reply to message {}", message);
        SendMessage notRegisteredMessage = new SendMessage();
        notRegisteredMessage.setChatId(message.getChatId());
        notRegisteredMessage.setText("Send /start before using the bot");
        executeFunction.execute(notRegisteredMessage);
    }

    public void getManageSubscriptionsMenu(Message message) throws TelegramApiException
    {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText("Choose the option in menu");
        sendMessage.setReplyMarkup(ButtonsService.getManageSubscriptionsMenu());
        executeFunction.execute(sendMessage);
    }

    public void removeRecording(CallbackQuery callbackQuery) throws TelegramApiException
    {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(callbackQuery.getMessage().getChatId());

        String messageId = callbackQuery.getData();

        int updatedRows = userService.removeRecordByUserIdAndMessageId(callbackQuery.getFrom().getId(), messageId);
        if (updatedRows > 0) {
            sendMessage.setText("Ok, removed");
        } else {
            sendMessage.setText("Recording not found");
        }
        executeFunction.execute(sendMessage);
    }

    public void returnMainMenu(Message message) throws TelegramApiException
    {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyMarkup(ButtonsService.getInitMenuButtons());
        sendMessage.setText("Choose one of the options");
        executeFunction.execute(sendMessage);
    }

//    public Pair<SendMessage, List<SendAudio>> handleText(Message message, ExecuteFunction execute) throws TelegramApiException {
//        SendMessage sendMessage = new SendMessage();
//        Long chatId = message.getChatId();
//        sendMessage.setChatId(chatId);
//
//        String inputMessage = message.getText();
//        if (CommandOptions.PULL.getCommand().equals(inputMessage)){
//            List<SendAudio> records = userService.pullAllRecordsForUser(message.getFrom().getId(), chatId);
//            return new Pair<>(null, records);
//        } else if (CommandOptions.FOLLOWERS.getCommand().equals(inputMessage)){
//            SendMessage followers = userService.getFollowers(message.getFrom().getId(), chatId);
//            return new Pair<>(followers, null);
//        } else if (CommandOptions.SUBSCRIPTIONS.getCommand().equals(inputMessage)){
//            SendMessage subscriptions = userService.getSubscriptions(message.getFrom().getId(), chatId);
//            return new Pair<>(subscriptions, null);
//        } else if (inputMessage.startsWith(CommandOptions.UNSUBSCRIBE.getCommand())) {
//            String followeeName = inputMessage.replaceAll(CommandOptions.UNSUBSCRIBE.getCommand(), "").trim();
//            SendMessage result;
//            if (followeeName.isEmpty()) {
//                result = new SendMessage();
//                result.setChatId(chatId);
//                result.setText("Specify username in form @user_name");
//            } else {
//                result = userService.unsubscribe(message, followeeName);
//            }
//            return new Pair<>(result, null);
//        } else if (inputMessage.startsWith(CommandOptions.REMOVE_FOLLOWER.getCommand())) {
//            String followerName = inputMessage.replaceAll(CommandOptions.REMOVE_FOLLOWER.getCommand(), "").trim();
//            SendMessage result;
//            if (followerName.isEmpty()) {
//                result = new SendMessage();
//                result.setChatId(chatId);
//                result.setText("Specify username in form @user_name");
//            } else {
//                result = userService.removeFollower(message, followerName, execute);
//            }
//            return new Pair<>(result, null);
//        } else if (CommandOptions.END.getCommand().equals(inputMessage)) {
//            SendMessage result = userService.removeUser(message.getFrom().getId(), chatId);
//            return new Pair<>(result, null);
//        } else if (inputMessage.startsWith(CommandOptions.HELP.getCommand())) {
//            SendMessage result = new SendMessage();
//            result.setChatId(chatId);
//            result.setText("This is help");
//            return new Pair<>(result, null);
//
//        } else {
//            sendMessage.setText("You can share only voice messages");
//        }
//        return new Pair<>(sendMessage, null);
//    }

//    public Pair<SendMessage, SendMessage> handleConfirmation(CallbackQuery callbackQuery)
//    {
//        String answer = callbackQuery.getData();
//
//        SendMessage sendMessage = new SendMessage();
//        sendMessage.setChatId(callbackQuery.getMessage().getChatId());
//        SendMessage messageToUser = new SendMessage();
//        Long foloweeId = callbackQuery.getFrom().getId();
//        Long userId = userService.getUserByFoloweeId(foloweeId);
//
//        if ("Yes".equals(answer))
//        {
//            userService.addContact(userId, foloweeId);
//            sendMessage.setText("User was accepted");
//            messageToUser.setChatId(userService.getChatIdByUserId(userId));
//            messageToUser.setText("User @" + callbackQuery.getFrom().getUserName() + " accepted you");
//        }
//
//        if ("No".equals(answer))
//        {
//            sendMessage.setText("Ok, subscribe request declined");
//            messageToUser.setChatId(userService.getChatIdByUserId(userId));
//            messageToUser.setText("User @" + callbackQuery.getFrom().getUserName() + " declined you");
//        }
//        return new Pair<>(sendMessage, messageToUser);
//    }
}
