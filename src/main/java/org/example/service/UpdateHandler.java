package org.example.service;

import org.example.Constants;
import org.example.bot.MyTelegramBot;
import org.example.enums.BotCommands;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Voice;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Component
public class UpdateHandler
{
    UserService userService;
    MyTelegramBot bot;

    @Autowired
    public UpdateHandler(UserService userService, @Lazy MyTelegramBot bot)
    {
        this.userService = userService;
        this.bot = bot;
    }

    public void handleVoiceMessage(Message message) throws TelegramApiException
    {
        Voice inputVoice = message.getVoice();

        String chatId = message.getChatId().toString();
        String fileId = inputVoice.getFileId();
        Long userId = message.getFrom().getId();
        userService.saveAudio(userId, fileId);

        SendMessage reply = new SendMessage();
        reply.setText("Ok, recorded");
        reply.setChatId(chatId);
        bot.execute(reply);
    }

    public void handleContact(Message message) throws TelegramApiException
    {
        Long userId = message.getFrom().getId();
        Long contactId = message.getUserShared().getUserId();
        Long chatId = message.getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        if (userService.getUserById(contactId) == null)
        {
            sendMessage.setText("Contact not found");
            bot.execute(sendMessage);
            return;
        }
        if (Integer.valueOf(1).equals(userService.getSubscriberByUserIdAndSubscriberId(userId, contactId)))
        {
            sendMessage.setText("Contact is already linked to user");
            bot.execute(sendMessage);
            return;
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

        bot.execute(sendMessage);
        bot.execute(messageForFolowee);
    }

    public void handleConfirmation(CallbackQuery callbackQuery) throws TelegramApiException
    {
        String answer = callbackQuery.getData();

        SendMessage messageToFollowee = new SendMessage();
        SendMessage messageToUser = new SendMessage();

        messageToFollowee.setChatId(callbackQuery.getMessage().getChatId());
        Long followeeId = callbackQuery.getFrom().getId();
        //TODO: bug below, need to know userId
        Long userId = userService.getUserByFoloweeId(followeeId);

        if (Constants.YES.equals(answer))
        {
            userService.addContact(userId, followeeId);
            messageToFollowee.setText("User was accepted");
            messageToUser.setChatId(userService.getChatIdByUserId(userId));
            messageToUser.setText("User @" + callbackQuery.getFrom().getUserName() + " accepted you");
        }

        if (Constants.No.equals(answer))
        {
            messageToFollowee.setText("Ok, subscribe request declined");
            messageToUser.setChatId(userService.getChatIdByUserId(userId));
            messageToUser.setText("User @" + callbackQuery.getFrom().getUserName() + " declined you");
        }
        bot.execute(messageToFollowee);
        bot.execute(messageToUser);
    }

    public void registerUser(Message message) throws TelegramApiException
    {
        int result = userService.addUser(message.getFrom().getId(), message.getChatId(), message.getFrom().getUserName());
        String replyMessage = result == 1 ? Constants.YOU_WAS_ADDED_TO_THE_SYSTEM : Constants.YOU_HAVE_ALREADY_REGISTERED;
        SendMessage sendMessage = new SendMessage(message.getChatId().toString(), replyMessage);
        sendMessage.setReplyMarkup(ButtonsService.getMenuButtons());
        bot.execute(sendMessage);
    }

    public void pull(Message message) throws TelegramApiException
    {
        List<SendVoice> records = userService.pullAllRecordsForUser(message.getFrom().getId(), message.getChatId());
        if (records.isEmpty())
        {
            bot.execute(new SendMessage(message.getChatId().toString(), "No updates"));
        }else {
            records.forEach(record -> {
                try
                {
                    bot.execute(record);
                }
                catch (TelegramApiException e)
                {
                    e.printStackTrace();
                }
            });
        }
    }

    public void getFollowers(Message message) throws TelegramApiException
    {
        bot.execute(userService.getFollowers(message.getFrom().getId(), message.getChatId()));
    }

    public void getSubscriptions(Message message) throws TelegramApiException
    {
        bot.execute(userService.getSubscriptions(message.getFrom().getId(), message.getChatId()));
    }

    public void unsubscribe(Message message) throws TelegramApiException
    {
        String followeeName = message.getText().replaceAll(BotCommands.UNSUBSCRIBE.getCommand(), "").trim();
        SendMessage result = new SendMessage();
        result.setChatId(message.getChatId());

        if (followeeName.isEmpty()) {
            result.setText("Specify username in form @user_name");
        } else {
            if (followeeName.startsWith("@")) {
                followeeName = followeeName.substring(1);
            }
            UserService.UserInfo followee = userService.loadUserInfoByName(followeeName);
            int updatedRows = userService.unsubscribe(message, followee);
            if (updatedRows > 0) {
                result.setText("Ok, unsubscribed");

                SendMessage unsubscribeNotification = new SendMessage();
                unsubscribeNotification.setChatId(followee.getChatId());
                unsubscribeNotification.setText("@" + message.getFrom().getUserName() + " unsubscribed");
                bot.execute(unsubscribeNotification);
            } else {
                result.setText("Nothing changed, are you really subscribed to @" + followeeName + "?");
            }
        }
        bot.execute(result);
    }

    public void removeFollower(Message message) throws TelegramApiException
    {
        String followerName = message.getText().replaceAll(BotCommands.REMOVE_FOLLOWER.getCommand(), "").trim();
        SendMessage result = new SendMessage();
        result.setChatId(message.getChatId());
        if (followerName.isEmpty()) {
            result.setText("Specify username in form @user_name");
        } else {
            if (followerName.startsWith("@")) {
                followerName = followerName.substring(1);
            }
            UserService.UserInfo follower = userService.loadUserInfoByName(followerName);
            int updatedRows = userService.removeFollower(message, follower);
            if (updatedRows > 0) {
                result.setText("Ok, user will no longer receive your updates");

                SendMessage unsubscribeNotification = new SendMessage();
                unsubscribeNotification.setChatId(follower.getChatId());
                unsubscribeNotification.setText("@" + message.getFrom().getUserName() + " revoked your subscription");
                bot.execute(unsubscribeNotification);
            } else {
                result.setText("Nothing changed, are you really subscribed to @" + followerName + "?");
            }
        }
        bot.execute(result);
    }

    public void end(Message message) throws TelegramApiException
    {
        bot.execute(userService.removeUser(message.getFrom().getId(), message.getChatId()));
    }

    public void help(Message message) throws TelegramApiException
    {
        bot.execute(new SendMessage(message.getChatId().toString(), "This is help"));
    }

    public void unsupportedResponse(Message message) throws TelegramApiException
    {
        bot.execute(new SendMessage(message.getChatId().toString(), "Only voice messages will be recorded"));
    }

    public void userNotRegistered(Message message) throws TelegramApiException
    {
        SendMessage notRegisteredMessage = new SendMessage();
        notRegisteredMessage.setChatId(message.getChatId());
        notRegisteredMessage.setText("Send /start to register before using the bot");
        bot.execute(notRegisteredMessage);
    }
}
