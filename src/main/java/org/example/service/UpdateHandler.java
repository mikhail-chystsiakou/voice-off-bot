package org.example.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.example.Constants;
import org.example.config.BotConfig;
import org.example.enums.MessageType;
import org.example.ffmpeg.FileInfo;
import org.example.model.UserInfo;
import org.example.model.VoicePart;
import org.example.repository.UserRepository;
import org.example.service.impl.UserServiceImpl;
import org.example.storage.FileStorage;
import org.example.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.GetUserProfilePhotos;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageCaption;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.stickers.Sticker;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.example.Constants.Messages.*;
import static org.example.Constants.Settings.*;
import static org.example.enums.Queries.*;
import static org.example.storage.FileStorage.DEFAULT_AUDIO_EXTENSION;
import static org.example.util.ThreadLocalMap.*;

@Component
@Slf4j
public class UpdateHandler {
    private static final String FILE_DATE_PATTERN = "yyyy" + File.separator + "MM" + File.separator + "dd_HH_mm_ss_SSS";

    @Autowired
    UserServiceImpl userService;

    @Autowired
    @Lazy
    ExecuteFunction executeFunction;

    @Autowired
    @Lazy
    SendAudioFunction sendAudioFunction;

    @Autowired
    @Lazy
    SendVideoFunction sendVideoFunction;

    @Autowired
    FileStorage fileStorage;

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

    @Autowired
    ButtonsService buttonsService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ThreadLocalMap tlm;

    public void storeMessageDescription(Message message, boolean sendConfirmation) throws TelegramApiException {
        int messageId = message.getMessageId();
        long userId = message.getFrom().getId();
        String description = message.getCaption();
        log.trace("Updated message description " +
                "for message id '{}' of user '{}' to '{}'", messageId, userId, description);
        jdbcTemplate.update(
                UPDATE_MESSAGE_DESCRIPTION.getValue(),
                description,
                userId,
                messageId
        );
        if (sendConfirmation) {
            SendMessage sm = new SendMessage();
            sm.setChatId(String.valueOf(message.getChatId()));
            sm.setText("Description updated");
            sm.setReplyToMessageId(messageId);
            executeFunction.execute(sm);
        }
    }

    public void handleVoiceMessage(Message message) throws TelegramApiException {
        Long userId = message.getFrom().getId();
        Integer replyModeMessageId = null;
        Long replyModeFolloweeId = null;

        if (message.getReplyToMessage() != null) {
            replyModeMessageId = message.getReplyToMessage().getMessageId();
            replyModeFolloweeId = userService.getFolloweeByPullMessage(replyModeMessageId);
        }

        if (replyModeMessageId != null) {
            if (!enableReplyMode(message)) {
                return;
            }
        }

        UserInfo userInfo = tlm.get(KEY_USER_INFO);
        if (userInfo != null && userInfo.getReplyModeMessageId() != null) {
            replyModeMessageId = userInfo.getReplyModeMessageId();
            replyModeFolloweeId = userInfo.getReplyModeFolloweeId();
        }
        Voice voice = message.getVoice();

        fileStorage.storeFile(
                message.getFrom().getId(),
                voice.getFileId(),
                voice.getDuration(),
                message.getMessageId(),
                replyModeFolloweeId,
                replyModeMessageId
        );

        if (message.getCaption() != null) {
            storeMessageDescription(message, false);
        }


        SendMessage reply = new SendMessage();

        String replyConfirm = tlm.get(KEY_REPLY_CONFIRM_TEXT);
        if (replyConfirm == null) {
            reply.setText(OK_RECORDED + ". Nobody pulled this recording yet");
        } else {
            reply.setText(replyConfirm + ". Author did not pulled this reply yet");
        }
        reply.setChatId(message.getChatId());
        reply.setReplyMarkup(buttonsService.getButtonForDeletingRecord(message.getMessageId()));
        Message replyMessage = executeFunction.execute(reply);
        jdbcTemplate.update(SET_OK_MESSAGE_ID.getValue(),
                replyMessage.getMessageId(),
                message.getFrom().getId(),
                message.getMessageId()
        );
        sendDelayNotifications(message.getFrom());
        sendInstantNotifications(message.getFrom());
    }

    private void sendInstantNotifications(User user)
    {
        userService.getUsersForInstantNotifications(user.getId()).forEach(userId -> {
            LocalTime localTime = LocalTime.now();
            userService.addUserNotification(userId, localTime);
        });
    }

    private void sendDelayNotifications(User user)
    {
        userService.getUsersForDelayNotifications(user.getId()).forEach((key, value) -> {
            LocalTime localTimeWithTimeZone = LocalTime.now(getTimeZoneByOffset(key).toZoneId());
            LocalTime localTime = LocalTime.now();

            log.info("localTimeWithTimeZone " + localTimeWithTimeZone);
            LocalTime estimatedTime = localTime.plusHours(12);
            LocalTime estimatedTimeWithTimeZone = localTimeWithTimeZone.plusHours(12);
            log.info("estimatedTimeWithTimeZone " + estimatedTimeWithTimeZone);
            int delta = 0;
            if (estimatedTimeWithTimeZone.getHour() < 9)
            {
                delta = 9 - estimatedTimeWithTimeZone.getHour();
            }
            else if (estimatedTimeWithTimeZone.getHour() > 21)
            {
                delta = 24 - estimatedTimeWithTimeZone.getHour() + 9;
            }
            log.info("delta " + delta);
            LocalTime estimatedTimeWithDelta = estimatedTime.plusHours(delta);
            log.info("estimatedTimeWithDelta" + estimatedTimeWithDelta);
            value.forEach(userId -> userService.addUserNotification(userId, estimatedTimeWithDelta));
        });
    }

    public void subscribeTo(Message message) throws TelegramApiException
    {
        long contactId = message.getUserShared().getUserId();
        long chatId = message.getChatId();

        subscribe(message.getFrom(), contactId, chatId);
    }

    public void subscribeToFromSuggestion(long chatId, String callbackData, User user) throws TelegramApiException
    {
        String[] data = callbackData.split("_");
        System.out.println("voly: " + callbackData);
        if (data.length > 1 && data[1] != null)
        {
            long contactId = Long.parseLong(data[1]);
            subscribe(user, contactId, chatId);
        }
    }

    public void handleConfirmation(CallbackQuery callbackQuery) throws TelegramApiException
    {
        String[] data = callbackQuery.getData().split("_");
        log.debug(Arrays.toString(data));
        System.out.println(callbackQuery.getData());
        System.out.println(Arrays.toString(data));

        String answer = data[0];
        Long userId = Long.valueOf(data[1]);

        SendMessage messageToFollowee = new SendMessage();
        SendMessage messageToUser = new SendMessage();
        messageToUser.setChatId(userService.getChatIdByUserId(userId));

        messageToFollowee.setChatId(callbackQuery.getMessage().getChatId());
        Long followeeId = callbackQuery.getFrom().getId();

        if (answer.contains(Constants.YES))
        {
            if (Integer.valueOf(1).equals(userService.getSubscriberByUserIdAndSubscriberId(userId, followeeId))){
                messageToFollowee.setText("The user is already following you");
                executeFunction.execute(messageToFollowee);
                return;
            }
            else {
                userService.addContact(userId, followeeId);
                messageToFollowee.setText(SUBSCRIBE_REQUEST_ACCEPT_CONFIRM);
                messageToUser.setText(MessageFormat.format(SUBSCRIBE_REQUEST_ACCEPTED,
                                                           getUserNameWithAt(callbackQuery)));
                messageToFollowee.setReplyMarkup(buttonsService.getFollowBackButton(userId,
                                                                                    !Integer.valueOf(1).equals(userService.getSubscriberByUserIdAndSubscriberId(followeeId, userId))));
            }
        }

        if (answer.contains(Constants.NO))
        {
            if (Integer.valueOf(1).equals(userService.getSubscriberByUserIdAndSubscriberId(userId, followeeId))){
                messageToFollowee.setText("Data already processed. The user is already following you. To unsubscribe user use *Subscriptions* -> *Remove subscriber*");
                messageToFollowee.enableMarkdown(true);
                executeFunction.execute(messageToFollowee);
                return;
            }
            else if (userService.getLatestRequestTimestamp(userId, followeeId) != null)
            {
                messageToFollowee.setText(SUBSCRIBE_REQUEST_DECLINE_CONFIRM);
                messageToUser.setText(MessageFormat.format(SUBSCRIBE_REQUEST_DECLINED,
                                                           getUserNameWithAt(callbackQuery)));
                userService.removeRequestToConfirm(userId, followeeId);
            }
            else {
                messageToUser.setText(MessageFormat.format(SUBSCRIBE_REQUEST_DECLINED,
                        getUserNameWithAt(callbackQuery)));
                messageToFollowee.setText("You already declined this user");
            }
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
        log.info("User registered: {}", user);

        redownloadUserPhoto(user.getId());

        if (result == 1)
        {
            sendTutorialRequest(message.getChatId());
        }
        else {
            SendMessage sendMessage = new SendMessage(message.getChatId().toString(), Constants.YOU_HAVE_ALREADY_REGISTERED);
            sendMessage.setReplyMarkup(buttonsService.getInitMenuButtons());
            executeFunction.execute(sendMessage);
        }

    }

    private void sendTutorialRequest(long chatId) throws TelegramApiException
    {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(buttonsService.getButtonsForTutorial());
        sendMessage.setText(Constants.YOU_WAS_ADDED_TO_THE_SYSTEM + "\nDo you want a quick guide?");
        executeFunction.execute(sendMessage);
    }

    private void redownloadUserPhoto(Long userId) throws TelegramApiException {
        log.trace("Trying to re-download profile photo of user with id '{}'", userId);
        GetUserProfilePhotos guph = new GetUserProfilePhotos();

        guph.setUserId(userId);
        guph.setOffset(0);
        guph.setLimit(1);
        UserProfilePhotos photos = executeFunction.execute(guph);

        if (photos.getPhotos().isEmpty()) {
            log.trace("No photos available for user '{}'", userId);
            return;
        }
        List<PhotoSize> photoSizes = photos.getPhotos().get(0);
        if (photoSizes.isEmpty()) {
            log.trace("No photo sizes available for user '{}'", userId);
            return;
        }
        PhotoSize photo = photoSizes.get(0);
        if (photo == null) {
            log.trace("No photo size available for user '{}'", userId);
            return;
        }

        if (!isUserPhotoExists(userId, photo.getFileId())){
            removePreviousImages(userId);
            saveImage(photo, userId);
        }
    }

    private void removePreviousImages(Long userId)
    {
        log.trace("Removing old images for user '{}'", userId);
        String folderPath = botConfig.getStoragePath() + botConfig.getProfilePicturesPath();
        File folder = new File(folderPath);
        final File[] files = folder.listFiles((dir,name) -> name.matches(userId.toString() + ".*?"));
        if (files == null) {
            log.trace("No old images for user '{}'", userId);
            return;
        }
        Arrays.stream(files).forEach((f) -> {
            boolean result = f.delete();
            log.debug("Removing old image of user {}, {}: {}", userId, f.getAbsolutePath(), result);
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
        log.debug("Copying profile picture from {} to {}", sourceFilename, destFilename);
        fileUtils.moveFileAbsolute(sourceFilename, destFilename);
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

    private String getContactUsername(Contact contact)
    {
        if (contact.getLastName() != null)
        {
            return contact.getFirstName() + " " + contact.getLastName();
        }
        return contact.getFirstName();
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

    private String getUserNameWithAt(User user)
    {
        if (user.getUserName() != null)
        {
            return "@" + user.getUserName();
        }
        if (user.getLastName() != null)
        {
            return user.getFirstName() + " " + user.getLastName();
        }
        return user.getFirstName();
    }

    public void pull(Message message) throws TelegramApiException
    {
        long userId = message.getFrom().getId();
        if (!pullProcessingSet.getAndSetPullProcessingForUser(userId)) {
            executeFunction.execute(new SendMessage(message.getChatId().toString(), "Preparing audios, please wait \uD83E\uDD2B"));
            return;
        }
        statsService.init();
        statsService.pullStart();
        statsService.setUserId(userId);

        redownloadUserPhoto(userId);
        changeUserName(message.getFrom());

        if (!userService.isDataAvailable(userId)){
            executeFunction.execute(new SendMessage(message.getChatId().toString(), "No updates \uD83E\uDD7A"));
            provideSubscriptionSuggestion(userId, message.getChatId());
        }
        else{
            executeFunction.execute(new SendMessage(message.getChatId().toString(), "Preparing audios for you \uD83E\uDD17..."));
            SendAudio record = userService.pullRecordForUser(userId, message.getChatId());
            try
            {
                statsService.pullEndBeforeUpload();
                Message pullMessage = sendAudioFunction.execute(record);
                storePullMessage(pullMessage);
                userService.cleanup(record.getFile().getNewMediaFile().getAbsolutePath());
            }
            catch (TelegramApiException e)
            {
                e.printStackTrace();
            }
            if (userService.isDataAvailable(message.getFrom().getId())) {
                executeFunction.execute(new SendMessage(message.getChatId().toString(), "More audios available \uD83D\uDE0F..."));
            }
            userService.deleteUserFromDelayNotification(message.getFrom().getId());
        }
        statsService.pullEnd();
        statsService.storePullStatistics();
        pullProcessingSet.finishProcessingForUser(userId);
    }

    @SneakyThrows
    private void provideSubscriptionSuggestion(long userId, long chatId) throws TelegramApiException
    {
        List<String> suggestions = Arrays.asList("Why not subscribe to @%s updates for daily inspiration?\uD83E\uDD14",
                                                 "We recommend you to subscribe to @%s. Don't miss out on inspirational and informative content.\uD83D\uDE01",
                                                 "Subscribe to @%s to make your feed more interesting!\uD83D\uDE43",
                                                 "We suggest an amazing @%s to subscribe to. Don't miss out!\uD83D\uDE0B",
                                                 "Want to expand your horizons? Subscribe to @%s!\uD83D\uDE09",
                                                 "Looking for like-minded individuals to connect with? Subscribe to @%s today!\uD83E\uDD24",
                                                 "You can add @%s to your network! Subscribe now!\uD83D\uDE00",
                                                 "Keep your social media feeds interesting and engaging by subscribing to @%s today!\uD83D\uDE42",
                                                 "Looking for some fresh content in your feed? Subscribe to @%s now!\uD83D\uDE07",
                                                 "Want to discover new and exciting accounts? Add @%s to your list of subscriptions!\uD83D\uDE01",
                                                 "Spice up your feed and stay up-to-date with @%s's content. Subscribe now!\uD83D\uDE0B");
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        Long followeeId = userService.getUserSuggestion(userId);
        if (followeeId != null)
        {
            UserInfo followee = userService.getUserById(followeeId);
            if (followee.getUsername() != null)
            {
                sendMessage.setText(String.format(suggestions.get(new Random().nextInt(suggestions.size())), followee.getUsername()));
                sendMessage.setReplyMarkup(ButtonsService.getUserSuggestionButton(followee.getUsername(), followeeId));
                executeFunction.execute(sendMessage);
            }
        }
    }

    private void storePullMessage(Message pullMessage) {
        String origMessageIds = tlm.get(KEY_ORIG_MESSAGE_IDS);
        Long followeeId = tlm.get(KEY_FOLLOWEE_ID);
        userService.storePullMessage(followeeId, pullMessage.getMessageId(), origMessageIds);
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

    public void storeFeedback(Message message) throws TelegramApiException {
        String feedbackText = null;
        if (message.hasText()) {
            feedbackText = message.getText();
        }

        String fileId = null;
        int duration = 0;
        String extension = ".bin";
        if (message.hasVoice()) {
            Voice voice = message.getVoice();
            fileId = voice.getFileId();
            duration = voice.getDuration();
            extension = DEFAULT_AUDIO_EXTENSION;
        } else if (message.hasAudio()) {
            Audio audio = message.getAudio();
            fileId = audio.getFileId();
            duration = audio.getDuration();
            extension = "mp3";
        } else if (message.hasDocument()) {
            Document document = message.getDocument();
            fileId = document.getFileId();
            extension = "doc";
        } else if (message.hasPhoto()) {
            List<PhotoSize> photos = message.getPhoto();
            PhotoSize photo = photos.get(0);
            for (PhotoSize ps : photos) {
                if (ps.getWidth() > photo.getWidth()) {
                    photo = ps;
                }
            }
            fileId = photo.getFileId();
            extension = "jpg";
        } else if (message.hasSticker()) {
            Sticker sticker = message.getSticker();
            PhotoSize photo = sticker.getThumb();
            fileId = photo.getFileId();
            extension = "jpg";
        } else if (message.hasVideo()) {
            Video video = message.getVideo();
            fileId = video.getFileId();
            duration = video.getDuration();
            extension = "mp4";

        }

        if (feedbackText == null && fileId == null) {
            executeFunction.execute(new SendMessage(message.getChatId().toString(), "Only text, audio, voice, photo, video and sticker feedbacks will be saved"));
            return;
        }

        if (fileId != null) {
            fileStorage.storeFile(message.getFrom().getId(), fileId, duration,
                    message.getMessageId(), extension, MessageType.FEEDBACK, null, null);
        }
        userService.storeFeedback(message.getFrom().getId(), message.getMessageId(), feedbackText, fileId);
        executeFunction.execute(new SendMessage(message.getChatId().toString(), "Thanks, feedback recorded \uD83E\uDEE1"));
    }

    public void unsubscribeFrom(Message message) throws TelegramApiException
    {
        unsubscribe(message.getFrom(), message.getUserShared().getUserId(), message.getChatId());
    }

    public void unsubscribe(User root, long followeeId, long chatId) throws TelegramApiException
    {
        SendMessage result = new SendMessage();
        result.setChatId(chatId);

        UserInfo followee = userRepository.findById(followeeId).get();
        if (followee == null)
        {
            result.setText(NOT_JOINED);
        }
        else
        {
            int updatedRows = userService.unsubscribe(root.getId(), followee);
            if (updatedRows > 0) {
                result.setText(MessageFormat.format(UNSUBSCRIBED, followee.getUserNameWithAt()));

                SendMessage unsubscribeNotification = new SendMessage();
                unsubscribeNotification.setChatId(followee.getChatId());
                unsubscribeNotification.setText(MessageFormat.format(SUBSCRIBER_UNSUBSCRIBED, getUserNameWithAt(root)));
                executeFunction.execute(unsubscribeNotification);
            } else {
                result.setText(MessageFormat.format(NOT_SUBSCRIBED, followee.getUserNameWithAt()));
            }
        }
        result.setReplyMarkup(buttonsService.getInitMenuButtons());
        executeFunction.execute(result);
    }

    public void enterReplyMode(Message message) {

    }

    public void removeSubscriber(Message message) throws TelegramApiException
    {
        Long userId = message.getUserShared().getUserId();
        SendMessage result = new SendMessage();
        result.setChatId(message.getChatId());
        UserInfo follower = userRepository.findById(userId).get();
        if (follower == null)
        {
            result.setText(NOT_JOINED);
        }
        else
        {
            int updatedRows = userService.removeFollower(message.getFrom().getId(), follower);
            if (updatedRows > 0) {
                result.setText(MessageFormat.format(SUBSCRIBER_REMOVED, follower.getUserNameWithAt()));

                SendMessage unsubscribeNotification = new SendMessage();
                unsubscribeNotification.setChatId(follower.getChatId());
                unsubscribeNotification.setText(MessageFormat.format(SUBSCRIPTION_REVOKED, getUserNameWithAt(message)));
                executeFunction.execute(unsubscribeNotification);
            } else {
                result.setText(MessageFormat.format(NOT_SUBSCRIBER, follower.getUserNameWithAt()));
            }
        }
        result.setReplyMarkup(buttonsService.getInitMenuButtons());
        executeFunction.execute(result);
    }

    public void removeSubscriberByRevoke(User root, long chatId, String callbackData) throws TelegramApiException
    {
        String[] data = callbackData.split("_");
        if (data.length > 1 && data[1] != null){
            Long userId = Long.parseLong(data[1]);

            SendMessage result = new SendMessage();
            result.setChatId(chatId);
            UserInfo follower = userRepository.findById(userId).get();
            if (follower == null)
            {
                result.setText(NOT_JOINED);
            }
            else
            {
                int updatedRows = userService.removeFollower(root.getId(), follower);
                if (updatedRows > 0) {
                    result.setText(MessageFormat.format(SUBSCRIBER_REMOVED, follower.getUserNameWithAt()));

                    SendMessage unsubscribeNotification = new SendMessage();
                    unsubscribeNotification.setChatId(follower.getChatId());
                    unsubscribeNotification.setText(MessageFormat.format(SUBSCRIPTION_REVOKED, getUserNameWithAt(root)));
                    executeFunction.execute(unsubscribeNotification);
                } else {
                    result.setText(MessageFormat.format(NOT_SUBSCRIBER, follower.getUserNameWithAt()));
                }
            }
            result.setReplyMarkup(buttonsService.getInitMenuButtons());
            executeFunction.execute(result);
        }
    }

    public void end(Message message) throws TelegramApiException
    {
        executeFunction.execute(userService.removeUser(message.getFrom().getId(), message.getChatId()));
    }

    public void help(Message message) throws TelegramApiException
    {
        executeFunction.execute(new SendMessage(message.getChatId().toString(), "This is help"));
    }

    public void unsupportedTextInput(Message message) throws TelegramApiException
    {
        executeFunction.execute(new SendMessage(message.getChatId().toString(), "Only voice messages will be recorded"));
    }

    public void userNotRegistered(Message message) throws TelegramApiException
    {
        log.trace("Sending not-registered warning reply to message {}", message);
        SendMessage notRegisteredMessage = new SendMessage();
        notRegisteredMessage.setChatId(message.getChatId());
        notRegisteredMessage.setText(SEND_START_FIRST);
        executeFunction.execute(notRegisteredMessage);
    }

    public void getManageSubscriptionsMenu(Message message) throws TelegramApiException
    {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setText(CHOOSE_OPTION_FROM_MENU);
        sendMessage.setReplyMarkup(buttonsService.getManageSubscriptionsMenu());
        executeFunction.execute(sendMessage);
    }

    public boolean enableReplyMode(Message message) throws TelegramApiException {
        long userId = message.getFrom().getId();

        UserInfo userInfo = tlm.get(KEY_USER_INFO);
        Long followeeId = userInfo.getReplyModeFolloweeId();
        if (followeeId == null && message.getReplyToMessage() != null) {
            followeeId = userService.getFolloweeByPullMessage(message.getReplyToMessage().getMessageId());
        }
        boolean isSubscriber =  userService.isSubscriber(followeeId, userId);
        log.debug("Trying to enable reply mode of {} to {}", followeeId, userId);
        if (isSubscriber) {
            changeReplyMode(message, true, "");
        } else {
            disableReplyMode(message, "You can't send replies to users that are not subscribed to you \uD83D\uDE4A");
        }
        return isSubscriber;
    }


    public void disableReplyMode(Message message) throws TelegramApiException {
        changeReplyMode(message, false, "");
    }

    public void disableReplyMode(Message message, String prefix) throws TelegramApiException {
        changeReplyMode(message, false, prefix);
    }

    public void changeReplyMode(Message message, boolean enableReplyMode, String prefix) throws TelegramApiException {
        long userId = message.getFrom().getId();
        Long replyModeFolloweeId = null;
        Integer replyModeMessageId = null;

        UserInfo userInfo = tlm.get(KEY_USER_INFO);
        boolean wasEnabled = userInfo.getReplyModeMessageId() != null;

        log.debug("WasEnabled: {}", wasEnabled);

        if (enableReplyMode && wasEnabled) {
            replyModeMessageId = userInfo.getReplyModeMessageId();
            replyModeFolloweeId = userInfo.getReplyModeFolloweeId();
        } else if (enableReplyMode) {
            replyModeMessageId = message.getReplyToMessage().getMessageId();
            replyModeFolloweeId = userService.getFolloweeByPullMessage(replyModeMessageId);
        }

        log.debug("replyModeFolloweeId: {}, replyMessageId: {}", replyModeFolloweeId, replyModeMessageId);
        userService.updateReplyEnabled(userId, replyModeFolloweeId, replyModeMessageId);
        userInfo.setReplyModeFolloweeId(replyModeFolloweeId);
        userInfo.setReplyModeMessageId(replyModeMessageId);

        String followeeName = "@username";
        if (replyModeFolloweeId != null) {
            UserInfo followee = userRepository.findById(replyModeFolloweeId).orElse(null);
            followeeName = followee.getUserNameWithAt();
        }

        String replyMessage = "";
        boolean sendSecondMessage = true;
        if (enableReplyMode && wasEnabled) {
            replyMessage = "Reply to " + followeeName + " recorded \uD83E\uDEE1";
            sendSecondMessage = false;
        } else if (enableReplyMode) {
            replyMessage = "Reply Mode enabled \uD83D\uDE4B\u200D♀️. All voice messages will be sent only to " + followeeName;
        } else if (wasEnabled) {
            replyMessage = prefix + "Reply Mode disabled \uD83D\uDE4A";
        } else {
            replyMessage = prefix;
            if (ObjectUtils.isEmpty(replyMessage)) {
                replyMessage = "Exited from Reply Mode";
            }
        }

        SendMessage sm = new SendMessage();
        sm.setChatId(message.getChatId());
        sm.setText(replyMessage);
        sm.setReplyMarkup(buttonsService.getInitMenuButtons());
        executeFunction.execute(sm);

        if (sendSecondMessage) {
            tlm.put(KEY_REPLY_CONFIRM_TEXT, "Reply to " + followeeName + " recorded \uD83E\uDEE1");
        }

    }

    public void enableFeedbackMode(Message message) throws TelegramApiException {
        changeFeedbackMode(message, true);
    }

    public void disableFeedbackMode(Message message) throws TelegramApiException {
        changeFeedbackMode(message, false);
    }

    public void changeFeedbackMode(Message message, boolean newState) throws TelegramApiException {
        long userId = message.getFrom().getId();
        userService.updateFeedbackEnabled(userId, newState);
        UserInfo userInfo = tlm.get(ThreadLocalMap.KEY_USER_INFO);
        if (userInfo == null) {
            log.warn("UserInfo is null", new RuntimeException());
        } else {
            userInfo.setFeedbackModeEnabled(newState);
        }
        SendMessage sm = new SendMessage();
        sm.setChatId(message.getChatId());
        if (newState) {
            sm.setText("Feedback Mode enabled. All inputs will be sent to BeWired team");
        } else {
            sm.setText("Feedback Mode disabled");
        }
        sm.setReplyMarkup(buttonsService.getInitMenuButtons());
        executeFunction.execute(sm);
    }



    public void removeRecording(CallbackQuery callbackQuery) throws TelegramApiException, IOException
    {
        if (callbackQuery.getData().contains("confirm"))
        {
            confirmRemovingRecording(callbackQuery);
        }
        else
        {
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(callbackQuery.getMessage().getChatId());

            if (callbackQuery.getData().contains("no"))
            {
                sendMessage.setText("OK");
            }
            else {
                String messageId = callbackQuery.getData().substring("remove_".length());

                Long userId = callbackQuery.getFrom().getId();
                log.debug("Removing message by id: {}, user: {}", messageId, userId);

                removeFileFromServer(userId, messageId);
                int updatedRows = userService.removeRecordByUserIdAndMessageId(userId, messageId);
                if (updatedRows > 0)
                {
                    sendMessage.setReplyToMessageId(Integer.valueOf(messageId));
                    sendMessage.setText(OK_REMOVED);
                }
                else
                {
                    sendMessage.setText(RECORDING_NOT_FOUND);
                }
            }
            executeFunction.execute(sendMessage);
        }
    }

    private void removeFileFromServer(Long userId, String messageId) throws IOException
    {
        FileInfo fileInfo = userService.getFileIdByUserAndMessageId(userId, Integer.valueOf(messageId));
        if (fileInfo != null)
        {
            java.sql.Date recordingTimestamp = new Date(fileInfo.getRecordingTimestamp());
            SimpleDateFormat sdf = new SimpleDateFormat(FILE_DATE_PATTERN);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            String recordingTimestampString = sdf.format(recordingTimestamp);
            String sourceFilePath = botConfig.getStoragePath() + botConfig.getVoicesPath() + File.separator + userId + File.separator
                + recordingTimestampString + "_" + fileInfo.getDuration() + "_" + fileInfo.getFileId() + "." + DEFAULT_AUDIO_EXTENSION;
            File fromFile = new File(sourceFilePath);

            if (fromFile.exists())
            {
                String pathToTrash = botConfig.getStoragePath() + botConfig.getVoicesPath() + File.separator + "trash" + File.separator + userId + File.separator;
                Files.createDirectories(Paths.get(pathToTrash));
                String targetFilePath = pathToTrash + fileInfo.getRecordingTimestamp() + "_" + fileInfo.getDuration() + "_" + fileInfo.getFileId() + ".opus";
                System.out.println("targetFilePath: " + targetFilePath);
                fileUtils.moveFileAbsolute(sourceFilePath, targetFilePath);
            }
        }
    }

    public void returnMainMenu(Message message) throws TelegramApiException
    {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyMarkup(buttonsService.getInitMenuButtons());
        sendMessage.setText(CHOOSE_OPTION_FROM_MENU);
        executeFunction.execute(sendMessage);
    }

    public void getSettings(Message message) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(message.getChatId());
        sendMessage.setReplyMarkup(buttonsService.getSettingsButtons());
        sendMessage.setText(SELECT_SETTING);
        executeFunction.execute(sendMessage);
    }

    public void setSettings(CallbackQuery callbackQuery, String callback) throws TelegramApiException {
        // show location
        Message message = callbackQuery.getMessage();
        if (callback.equals(SETTING_TIMEZONE) || callback.equals(SETTING_TIMEZONE + "_7")) {
            System.out.println("callback : " + callback);
            EditMessageReplyMarkup emrm = new EditMessageReplyMarkup();
            emrm.setMessageId(message.getMessageId());
            emrm.setReplyMarkup(buttonsService.getTimezonesButtons(callback.equals(SETTING_TIMEZONE + "_7")));
            emrm.setChatId(message.getChatId());
            executeFunction.execute(emrm);
        } else if (callback.startsWith(SETTING_TIMEZONE)) {
            System.out.println(callback);
            Pattern p = Pattern.compile(SETTING_TIMEZONE + "_(-?\\d\\d\\d\\d)");
            Matcher m = p.matcher(callback);
            m.find();
            String timezone = m.group(1);
            System.out.println("Selected timezone: " + timezone + ", userId = " + callbackQuery.getFrom().getId());
            jdbcTemplate.update(UPDATE_TIMEZONE.getValue(), Long.parseLong(timezone), callbackQuery.getFrom().getId());
            executeFunction.execute(new SendMessage(
                    message.getChat().getId().toString(),
                    "Ok, timezone updated to " + getTimeZoneByOffset(Integer.parseInt(timezone)).getDisplayName()
            ));

            if (callback.endsWith("f")) {
                SendMessage sendMessage = new SendMessage();

                sendMessage.setChatId(message.getChatId());
                sendMessage.setText("*Congratulations, you are ready to go!* \n\nShare yourself. It's valuable 🤗. \n" + getTextForSubscriptionsSuggestions(callbackQuery.getFrom().getId()));
                sendMessage.setReplyMarkup(buttonsService.getInitMenuButtons());
                sendMessage.setParseMode("Markdown");

                executeFunction.execute(sendMessage);
            }

            EditMessageReplyMarkup emrm = new EditMessageReplyMarkup();
            emrm.setMessageId(message.getMessageId());
            emrm.setChatId(message.getChatId());
            executeFunction.execute(emrm);
        } else if (callback.startsWith(SETTING_NOTIFICATIONS)) {
            if (SETTING_NOTIFICATIONS_INSTANT.equals(callback)){
                userService.updateNotificationSettings(callbackQuery.getFrom().getId(), 1);
                userService.deleteUserFromDelayNotification(callbackQuery.getFrom().getId());
                executeFunction.execute(new SendMessage(message.getChatId().toString(), "Notification settings updated"));
            }else if (SETTING_NOTIFICATIONS_PULL.equals(callback)) {
                userService.updateNotificationSettings(callbackQuery.getFrom().getId(),0);
                userService.deleteUserFromDelayNotification(callbackQuery.getFrom().getId());
                executeFunction.execute(new SendMessage(message.getChatId().toString(), "Notification settings updated"));
            }else if (SETTING_NOTIFICATIONS_ONCE_A_DAY.equals(callback)) {
                userService.updateNotificationSettings(callbackQuery.getFrom().getId(),2);
                userService.deleteUserFromDelayNotification(callbackQuery.getFrom().getId());
                executeFunction.execute(new SendMessage(message.getChatId().toString(), "Notification settings updated"));
            }
            else {
                EditMessageReplyMarkup emrm = new EditMessageReplyMarkup();
                emrm.setMessageId(message.getMessageId());
                emrm.setChatId(message.getChatId());
                emrm.setReplyMarkup(ButtonsService.getNotificationSettingsButtons());
                executeFunction.execute(emrm);
            }
        } else if (callback.startsWith(SETTING_FEEDBACK)) {
            boolean feedbackAllowed = SETTING_FEEDBACK_ALLOWED.equals(callback);
            userService.updateFeedbackAllowed(callbackQuery.getFrom().getId(), feedbackAllowed);
            boolean feedbackModeDisabled = false;
            UserInfo userInfo = tlm.get(ThreadLocalMap.KEY_USER_INFO);
            if (userInfo == null) {
                log.warn("User info is null", new RuntimeException());
            } else {
                userInfo.setFeedbackModeAllowed(feedbackAllowed);
                if (!feedbackAllowed) {
                    userInfo.setFeedbackModeEnabled(false);
                    userService.updateFeedbackEnabled(callbackQuery.getFrom().getId(), false);
                    feedbackModeDisabled = true;
                }
                tlm.put(ThreadLocalMap.KEY_USER_INFO, userInfo);
            }

            EditMessageReplyMarkup emrm = new EditMessageReplyMarkup();
            emrm.setMessageId(message.getMessageId());
            emrm.setChatId(message.getChatId());
            emrm.setReplyMarkup(buttonsService.getSettingsButtons());
            executeFunction.execute(emrm);

            String replyText = "Ok, feedback mode " + (feedbackAllowed ? "allowed" : "prohibited");
            if (feedbackModeDisabled) replyText += " and disabled";

            SendMessage reply = new SendMessage();
            reply.setText(replyText);
            reply.setChatId(message.getChatId());
            reply.setReplyMarkup(buttonsService.getInitMenuButtons());
            executeFunction.execute(reply);
        }
    }

    public void showHideTimestamps(CallbackQuery callbackQuery, String callback) throws TelegramApiException {
        Message message = callbackQuery.getMessage();
        String[] data = callbackQuery.getData().split("_");
        System.out.println("callback data: " + callbackQuery.getData());

        long followeeId = data.length > 2 ? Long.parseLong(data[2]) : 0;
        boolean isReply = data.length > 3;
        System.out.println("new f: " + followeeId);

        EditMessageReplyMarkup emrm = new EditMessageReplyMarkup();
        emrm.setMessageId(message.getMessageId());
        emrm.setReplyMarkup(buttonsService.getShowTimestampsButton(followeeId, isReply));
        emrm.setChatId(message.getChatId());

        EditMessageCaption emc = new EditMessageCaption();
        emc.setChatId(message.getChatId());
        emc.setMessageId(message.getMessageId());

        if (callback.startsWith("timestamps_show")) {
            long userId = callbackQuery.getFrom().getId();
            UserInfo userInfo = userRepository.findById(userId).get();
            System.out.println("callback date: " + callbackQuery.getMessage().getDate());
            long audioMessageTime = callbackQuery.getMessage().getDate() * 1000L;
            // 2104732264000
            System.out.println("prev date: " + new Timestamp(audioMessageTime) + " - " + audioMessageTime);
            System.out.println("userId: " + userId);
            Triplet<Long, Long, Long> getPullTimestamps = followeeId == 0 ? getPreviousPullTimestamp(userId, audioMessageTime) : getPreviousPullTimestampByFolloweeId(userId, followeeId, audioMessageTime) ;

            System.out.println("followee_id: " + getPullTimestamps.getFirst());
            System.out.println("pull_timestamp: " + getPullTimestamps.getSecond());
            System.out.println("last_pull_timestamp: " + getPullTimestamps.getThird());
            // +1s because time is truncated
            List<VoicePart> voiceParts = loadVoiceParts(
                    getPullTimestamps.getFirst(),
                    getPullTimestamps.getSecond()  + 1000L,
                    getPullTimestamps.getThird(),
                    isReply
            );
            System.out.println(Arrays.toString(voiceParts.toArray()));
            String caption = "";
            if (voiceParts.size() > 1
                    || (!voiceParts.isEmpty() && voiceParts.get(0).description != null)) {
                caption = getAudioCaption(voiceParts, userInfo.getTimezone());
            }
            System.out.println("new caption: " + caption);

            emc.setCaption(caption);
            emc.setParseMode("Markdown");
            emrm.setReplyMarkup(buttonsService.getHideTimestampsButton(followeeId, isReply));

        } else if (callback.startsWith("timestamps_hide")) {
            emc.setCaption("");
            emrm.setReplyMarkup(buttonsService.getShowTimestampsButton(followeeId, isReply));
        }
        try{
            executeFunction.execute(emc);
        }
        catch (Exception e){
            if (e.getMessage().contains("MEDIA_CAPTION_TOO_LONG"))
            {
                String caption = emc.getCaption();
                emc.setCaption(caption.substring(0, 1021) + "...");
                executeFunction.execute(emc);
            }
            log.info("error in showHideTimestamps(): " + e.getMessage());
        }
        executeFunction.execute(emrm);
    }

    private Triplet<Long, Long, Long> getPreviousPullTimestampByFolloweeId(long userId, long followeeId, long nextTimestamp)
    {
        return jdbcTemplate.query(GET_PREVIOUS_PULL_TIMESTAMP_BY_FOLLOWEE.getValue(),
                                  (rs) -> {
                                      Triplet<Long, Long, Long> triplet = new Triplet<>();
                                      if (rs.next())
                                      {
                                          triplet.setFirst(rs.getLong("followee_id"));
                                          triplet.setSecond(rs.getTimestamp("pull_timestamp").getTime());
                                          triplet.setThird(rs.getTimestamp("last_pull_timestamp").getTime());
                                      }
                                      return triplet;
                                  }, new Timestamp(nextTimestamp), userId, followeeId);
    }

    private Triplet<Long, Long, Long> getPreviousPullTimestamp(long userId, long nextTimestamp) {
        return jdbcTemplate.query(GET_PREVIOUS_PULL_TIMESTAMP.getValue(),
                (rs) -> {
                    Triplet<Long, Long, Long> triplet = new Triplet<>();
                    if (rs.next()) {
                        triplet.setFirst(rs.getLong("followee_id"));
                        triplet.setSecond(rs.getTimestamp("pull_timestamp").getTime());
                        triplet.setThird(rs.getTimestamp("last_pull_timestamp").getTime());
                    }
                    return triplet;
                }, new Timestamp(nextTimestamp), userId);
    }

    private List<VoicePart> loadVoiceParts(long userId, Long pullTimestamp, Long lastPullTimestamp, boolean isReply) {
        if (pullTimestamp == null || lastPullTimestamp == null) return Collections.emptyList();

        System.out.println("Loading voice parts for user " + userId + ", " + new Timestamp(pullTimestamp) + " - " + new Timestamp(lastPullTimestamp) );

        String query = isReply ? GET_VOICE_PARTS_BY_TIMESTAMPS_WITH_REPLY.getValue() : GET_VOICE_PARTS_BY_TIMESTAMPS.getValue();

        return jdbcTemplate.queryForStream(query,
                (rs, rn) -> {
                    VoicePart voicePart = new VoicePart();
                    voicePart.duration = rs.getLong("duration");
                    voicePart.description = rs.getString("description");
                    voicePart.recordingTimestamp = rs.getTimestamp("recording_timestamp").getTime();
                    return voicePart;
                }, userId, new Timestamp(lastPullTimestamp), new Timestamp(pullTimestamp)).collect(Collectors.toList());
    }

    private String getAudioCaption(List<VoicePart> voiceParts, int zoneOffset) {
        long start = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("`yyyy.MM.dd, HH:mm`");
        sdf.setTimeZone(getTimeZoneByOffset(zoneOffset));
        StringJoiner sj = new StringJoiner("\n");
        for (VoicePart vp : voiceParts) {

            String timeHandle = getTimeHandle(start);
            String recordingTimestamp = sdf.format(new Timestamp(vp.recordingTimestamp));
            String voiceDescription = vp.getDescription();
            voiceDescription = voiceDescription == null ? "" : "\n" + voiceDescription;

            String caption = timeHandle + " - " + recordingTimestamp + voiceDescription;
            sj.add(caption);

            start += vp.duration;
        }
        return "\n" + sj.toString();
    }

    private String getTimeHandle(long start) {
        long hours = start / 3600;
        long seconds = start % 60;
        long minutes = (start - (hours * 3600)) / 60;
        String timeHandle = "";
        if (hours > 0) {
            timeHandle += hours + ":";
        }
        timeHandle += String.format("%02d:%02d", minutes, seconds);
        return timeHandle;
    }

    private TimeZone getTimeZoneByOffset(int zoneOffset) {
        String zoneId = "GMT";
        if (zoneOffset < 0) {
            zoneId += "-";
        } else {
            zoneId += "+";
        }
        zoneId += zoneOffset / 100;
        zoneId += ":";
        zoneId += String.format("%02d", zoneOffset % 100);
        TimeZone tz = TimeZone.getTimeZone(zoneId);
        System.out.println("timezone: " + tz.getDisplayName() + ", from " + zoneId);
        return tz;
    }

    public void getTutorial(long chatId, int stage, long userId) throws TelegramApiException
    {
        SendVideo sendVideo = new SendVideo();
        sendVideo.setChatId(chatId);

        if (stage == 1)
        {
            sendVideo.setCaption("*Subscribe* \uD83D\uDE4B\u200D♀️ to your friend and wait for *confirmation* \uD83D\uDC4D");
            sendVideo.setVideo(new InputFile(new File("/mnt/bewired/resources/BeWired1.mp4")));
            sendVideo.setReplyMarkup(buttonsService.getNextButton(stage));
            sendVideo.setParseMode("Markdown");

            sendVideoFunction.execute(sendVideo);
        }
        else if (stage == 2)
        {
            sendVideo.setCaption("*Record* \uD83C\uDF99 a voice message to share your thoughts with friends" +
                                     "\n - Don't worry if you don't succeed. You can *remove* \uD83D\uDDD1 record any time" +
                                     "\n - You can record by *small pieces* \uD83E\uDDE9 " +
                    "- everything is concatenated *into single file* \uD83D\uDE0C for your friends ");
            sendVideo.setVideo(new InputFile(new File("/mnt/bewired/resources/BeWired2.mp4")));
            sendVideo.setParseMode("Markdown");
            sendVideo.setReplyMarkup(buttonsService.getNextButton(stage));

            sendVideoFunction.execute(sendVideo);
        }
        else if (stage == 3)
        {
            sendVideo.setCaption("*Pull* \uD83E\uDEF4 voices of your friends");
            sendVideo.setVideo(new InputFile(new File("/mnt/bewired/resources/BeWired3.mp4")));
            sendVideo.setReplyMarkup(buttonsService.getNextButton(stage));
            sendVideo.setParseMode("Markdown");

            sendVideoFunction.execute(sendVideo);
        }
        else if (stage == 4)
        {
            sendVideo.setCaption("You can add a *description* \uD83D\uDCDD to each recording");
            sendVideo.setVideo(new InputFile(new File("/mnt/bewired/resources/BeWired4.mp4")));
            sendVideo.setReplyMarkup(buttonsService.getNextButton(stage));
            sendVideo.setParseMode("Markdown");

            sendVideoFunction.execute(sendVideo);
        }
        else if (stage == 5)
        {
            sendVideo.setCaption("Use *Reply Mode*! You can record \uD83C\uDFA4 replies on audios during the listening\n" +
                                     "The author will get all your replies on next pull \uD83D\uDCE2\n" +
                                     "\n" +
                                     "P.S. You can even record replies on replies \uD83D\uDE0F");
            sendVideo.setVideo(new InputFile(new File("/mnt/bewired/resources/BeWired5.mp4")));
            sendVideo.setReplyMarkup(buttonsService.getNextButton(stage));
            sendVideo.setParseMode("Markdown");

            sendVideoFunction.execute(sendVideo);
        }
        else if (stage == 6)
        {
            SendMessage lastStep = new SendMessage();
            lastStep.setText("*Setup your timezone* \uD83C\uDF0E for proper date and time handling. " +
                    "You can change it later via /settings command \uD83D\uDEE0");
            lastStep.setParseMode("Markdown");
            lastStep.setChatId(chatId);
            lastStep.setReplyMarkup(buttonsService.getTimezoneMarkup(stage));
            executeFunction.execute(lastStep);
        }
        else if (stage == 7) {

            SendMessage sendMessage = new SendMessage();

            sendMessage.setChatId(chatId);
            sendMessage.setText("*Congratulations, you are ready to go!* ⛷ \n\nShare yourself. It's valuable 🤗. \n" + getTextForSubscriptionsSuggestions(userId));
            sendMessage.setReplyMarkup(buttonsService.getInitMenuButtons());
            sendMessage.setParseMode("Markdown");

            executeFunction.execute(sendMessage);
        }
    }

    public void declineTutorial(long chatId, long userId) throws TelegramApiException
    {
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.setType("bot_command");
        messageEntity.setOffset(65);
        messageEntity.setLength(9);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setEntities(Arrays.asList(messageEntity));
        sendMessage.setText("Ok, you can go through the tutorial whenever you want using the /tutorial command \uD83D\uDEE0." +
                " For now you are ready to go! ⛷\n\nShare yourself. It's valuable 🤗. \n" + getTextForSubscriptionsSuggestions(userId));
        sendMessage.setReplyMarkup(buttonsService.getInitMenuButtons());
        executeFunction.execute(sendMessage);
    }

    private String getTextForSubscriptionsSuggestions(long userId){
        return "You can subscribe on the most active users: \n" + userService.getListOfTheMostActiveUsers(userId).stream()
            .map(name -> "@" + name)
            .collect(Collectors.joining("\n"));
    }

    public void confirmRemovingRecording(CallbackQuery callbackQuery) throws TelegramApiException
    {
        String messageId = callbackQuery.getData().substring("confirmremove_".length());

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(callbackQuery.getMessage().getChatId());
        sendMessage.setText("Do you really want to delete the recording?");
        sendMessage.setReplyMarkup(ButtonsService.getRemovingConfirmationButtons(Integer.valueOf(messageId)));
        executeFunction.execute(sendMessage);
    }

    public void subscribe(User userFrom, long to, long fromChatId) throws TelegramApiException
    {
        long from = userFrom.getId();
        System.out.println("from : " + from + ". to: " + to);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(fromChatId);
        sendMessage.setReplyMarkup(buttonsService.getInitMenuButtons());

        if (userService.getUserIdById(to) == null)
        {
            sendMessage.setText(NOT_JOINED);
            executeFunction.execute(sendMessage);
            return;
        }
        if (Integer.valueOf(1).equals(userService.getSubscriberByUserIdAndSubscriberId(from, to)))
        {
            UserInfo contact = userRepository.findById(to).orElse(null);
            sendMessage.setText(MessageFormat.format(ALREADY_SUBSCRIBED, contact.getUserNameWithAt()));
            executeFunction.execute(sendMessage);
            return;
        }
        UserInfo userInfo = userRepository.findById(to).orElse(null);
        Timestamp latestRequestTimestamp = userService.getLatestRequestTimestamp(from, to);
        long utcDayAgo = Instant.now().minus(1, ChronoUnit.DAYS).atOffset(ZoneOffset.UTC).toInstant().toEpochMilli();
        long latestRequestMillis = 0;
        if (latestRequestTimestamp != null) {
            latestRequestMillis = latestRequestTimestamp.getTime();
        }
        long latestRequestPlusDay = Instant.ofEpochMilli(latestRequestMillis).plus(1, ChronoUnit.DAYS).toEpochMilli();
        long utcNow = Instant.now().atOffset(ZoneOffset.UTC).toInstant().toEpochMilli();

        if (latestRequestPlusDay < utcNow) {
            SendMessage messageForFollowee = new SendMessage();
            messageForFollowee.setChatId(userInfo.getChatId());
            messageForFollowee.setText(MessageFormat.format(
                SUBSCRIBE_REQUEST_QUESTION, getUserNameWithAt(userFrom)));
            messageForFollowee.setReplyMarkup(buttonsService.getInlineKeyboardMarkupForSubscription(from));

            sendMessage.setText(MessageFormat.format(SUBSCRIBE_REQUEST_SENT, userInfo.getUserNameWithAt()));

            if (!Integer.valueOf(1).equals(userService.getLatestRequestTimestamp(from, to))){
                System.out.println("from : " + from + ". to: " + to);
                int result = userService.addRequestToConfirm(from, to);
            }

            executeFunction.execute(sendMessage);
            executeFunction.execute(messageForFollowee);
        } else {
            sendMessage.setText(REQUEST_ALREADY_SENT);
            executeFunction.execute(sendMessage);
        }
    }
}
