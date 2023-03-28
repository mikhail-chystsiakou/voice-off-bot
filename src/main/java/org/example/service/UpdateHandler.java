package org.example.service;

import org.example.util.Pair;
import org.example.enums.CommandOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
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

    public SendVoice handleVoiceMessage(Message message)
    {
        Voice inputVoice = message.getVoice();
        String chatId = message.getChatId().toString();
        String fileId = inputVoice.getFileId();
        Long userId = message.getFrom().getId();
        userService.saveAudio(userId, fileId);

        SendVoice voice = new SendVoice();
        InputFile inputFile = new InputFile(fileId);
        voice.setChatId(chatId);
        voice.setVoice(inputFile);

        return voice;
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

        InlineKeyboardButton inlineKeyboardButtonYes = new InlineKeyboardButton();
        inlineKeyboardButtonYes.setText("Yes");
        inlineKeyboardButtonYes.setCallbackData("Yes");

        InlineKeyboardButton inlineKeyboardButtonNo = new InlineKeyboardButton();
        inlineKeyboardButtonNo.setText("No");
        inlineKeyboardButtonNo.setCallbackData("No");


        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(Arrays.asList(Arrays.asList(inlineKeyboardButtonYes, inlineKeyboardButtonNo)));

        messageForFolowee.setReplyMarkup(inlineKeyboardMarkup);
        sendMessage.setText("Your request was sent");

        if (!Integer.valueOf(1).equals(userService.getRequestRecord(userId, contactId))){
            int result = userService.addRequestToConfirm(userId, contactId);
        }

        return new Pair<>(sendMessage, messageForFolowee);
    }

    public SendMessage handleText(Message message)
    {
        SendMessage sendMessage = new SendMessage();
        Long chatId = message.getChatId();
        sendMessage.setChatId(chatId);


        String textMessage = message.getText();
        if (CommandOptions.START.getValue().equals(textMessage)){
            int result = userService.addUser(message.getFrom().getId(), chatId);
            String replyMessage = result == 1 ? "You was added to the system" : "You have already registered";
            sendMessage.setText(replyMessage);
        }
        else {
            sendMessage.setText(textMessage);
        }
        return sendMessage;
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
            messageToUser.setText("User @" + callbackQuery.getFrom().getUserName() + " accepted you!");
        }

        if ("No".equals(answer))
        {
            messageToUser.setChatId(userService.getChatIdByUserId(userId));
            messageToUser.setText("User @" + callbackQuery.getFrom().getUserName() + " decline you!");
        }
        return new Pair<>(sendMessage, messageToUser);
    }
}
