package org.example.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Voice;

public class UpdateHandler
{
    Message message;

    public UpdateHandler(Message message)
    {
        this.message = message;
    }

    public SendVoice handleVoiceMessage()
    {
        Voice inputVoice = message.getVoice();
        String chatId = message.getChatId().toString();

        // Download the voice message
        String fileId = inputVoice.getFileId();
//            String filePath = downloadVoiceMessage(fileId);

        // Send the voice message back to the user
        SendVoice voice = new SendVoice();
        InputFile inputFile = new InputFile(fileId);
        voice.setChatId(chatId);
        voice.setVoice(inputFile);
        return voice;
    }

    public SendMessage handleContact()
    {
        Long userId = message.getFrom().getId();
        Long contactId = message.getContact().getUserId();
        Long chatId = message.getChatId();
        new UserService().addContact(userId, contactId);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText("Contact was added");
        return sendMessage;
    }

    public SendMessage handleText()
    {
        String textMessage = message.getText();
        Long chatId = message.getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(textMessage);
        return sendMessage;
    }
}
