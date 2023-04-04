package org.example.service;

import org.example.util.ExecuteFunction;
import org.example.enums.CommandOptions;
import org.example.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Voice;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
public class UpdateHandler
{
    UserService userService;

    @Autowired
    public UpdateHandler(UserService userService)
    {
        this.userService = userService;
    }

    public SendMessage handleVoiceMessage(Message message)
    {
        Voice inputVoice = message.getVoice();

        String chatId = message.getChatId().toString();
        String fileId = inputVoice.getFileId();
        Long userId = message.getFrom().getId();
        userService.saveAudio(userId, fileId);

        SendMessage reply = new SendMessage();
        reply.setText("Ok, recorded");
        reply.setChatId(chatId);
        return reply;
    }

    public Pair<SendMessage, SendMessage> handleContact(Message message)
    {
        Long userId = message.getFrom().getId();
        Long contactId = message.getContact().getUserId();
        Long chatId = message.getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        if (userService.getUserById(contactId) == null)
        {
            sendMessage.setText("Contact not found");
            return new Pair<>(sendMessage, null);
        }
        if (Integer.valueOf(1).equals(userService.getSubscriberByUserIdAndSubscriberId(userId, contactId)))
        {
            sendMessage.setText("Contact is already linked to user");
            return new Pair<>(sendMessage, null);
        }
        Long foloweeChatId = userService.getChatIdByUserId(contactId);
        SendMessage messageForFolowee = new SendMessage();
        messageForFolowee.setChatId(foloweeChatId);
        messageForFolowee.setText("Hi! @" + message.getFrom().getUserName() + " send request to follow you. Do you confirm?");
        messageForFolowee.setReplyMarkup(ButtonsService.getInlineKeyboardMarkupForSubscription());

        sendMessage.setText("Your request was sent");

        if (!Integer.valueOf(1).equals(userService.getRequestRecord(userId, contactId))){
            int result = userService.addRequestToConfirm(userId, contactId);
        }

        return new Pair<>(sendMessage, messageForFolowee);
    }

    public Pair<SendMessage, List<SendVoice>> handleText(Message message, ExecuteFunction execute) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        Long chatId = message.getChatId();
        sendMessage.setChatId(chatId);

        String inputMessage = message.getText();
        if (CommandOptions.START.getCommand().equals(inputMessage)){
            int result = userService.addUser(message.getFrom().getId(), chatId, message.getFrom().getUserName());
            String replyMessage = result == 1 ? "You was added to the system. Subscribe to other person by sharing it's contact here." : "You have already registered";
            sendMessage.setText(replyMessage);
        } else if (CommandOptions.PULL.getCommand().equals(inputMessage)){
            List<SendVoice> records = userService.pullAllRecordsForUser(message.getFrom().getId(), chatId);
            return new Pair<>(null, records);
        } else if (CommandOptions.FOLLOWERS.getCommand().equals(inputMessage)){
            SendMessage followers = userService.getFollowers(message.getFrom().getId(), chatId);
            return new Pair<>(followers, null);
        } else if (CommandOptions.SUBSCRIPTIONS.getCommand().equals(inputMessage)){
            SendMessage subscriptions = userService.getSubscriptions(message.getFrom().getId(), chatId);
            return new Pair<>(subscriptions, null);
        } else if (inputMessage.startsWith(CommandOptions.UNSUBSCRIBE.getCommand())) {
            String followeeName = inputMessage.replaceAll(CommandOptions.UNSUBSCRIBE.getCommand(), "").trim();
            SendMessage result;
            if (followeeName.isEmpty()) {
                result = new SendMessage();
                result.setChatId(chatId);
                result.setText("Specify username in form @user_name");
            } else {
                result = userService.unsubscribe(message, followeeName, execute);
            }
            return new Pair<>(result, null);
        } else if (inputMessage.startsWith(CommandOptions.REMOVE_FOLLOWER.getCommand())) {
            String followerName = inputMessage.replaceAll(CommandOptions.REMOVE_FOLLOWER.getCommand(), "").trim();
            SendMessage result;
            if (followerName.isEmpty()) {
                result = new SendMessage();
                result.setChatId(chatId);
                result.setText("Specify username in form @user_name");
            } else {
                result = userService.removeFollower(message, followerName, execute);
            }
            return new Pair<>(result, null);
        } else if (CommandOptions.END.getCommand().equals(inputMessage)) {
            SendMessage result = userService.removeUser(message.getFrom().getId(), chatId);
            return new Pair<>(result, null);
        } else if (inputMessage.startsWith(CommandOptions.HELP.getCommand())) {
            SendMessage result = new SendMessage();
            result.setChatId(chatId);
            result.setText("This is help");
            return new Pair<>(result, null);

        } else {
            sendMessage.setText("Only voice messages will be recorded");
        }
        return new Pair<>(sendMessage, null);
    }

    public Pair<SendMessage, SendMessage> handleConfirmation(CallbackQuery callbackQuery)
    {
        String answer = callbackQuery.getData();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(callbackQuery.getMessage().getChatId());
        SendMessage messageToUser = new SendMessage();
        Long foloweeId = callbackQuery.getFrom().getId();
        Long userId = userService.getUserByFoloweeId(foloweeId);

        if ("Yes".equals(answer))
        {
            userService.addContact(userId, foloweeId);
            sendMessage.setText("User was accepted");
            messageToUser.setChatId(userService.getChatIdByUserId(userId));
            messageToUser.setText("User @" + callbackQuery.getFrom().getUserName() + " accepted you");
        }

        if ("No".equals(answer))
        {
            sendMessage.setText("Ok, subscribe request declined");
            messageToUser.setChatId(userService.getChatIdByUserId(userId));
            messageToUser.setText("User @" + callbackQuery.getFrom().getUserName() + " declined you");
        }
        return new Pair<>(sendMessage, messageToUser);
    }
}
