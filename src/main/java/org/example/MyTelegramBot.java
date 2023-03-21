package org.example;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.Properties;

public class MyTelegramBot extends TelegramLongPollingBot {

    // Replace with your bot token
    private static String BOT_TOKEN;

    public MyTelegramBot() throws IOException {
        Properties secretProperties = new Properties();
        secretProperties.load(
                ClassLoader.getSystemClassLoader().getResourceAsStream("secrets.properties")
        );
        BOT_TOKEN = secretProperties.getProperty("TG_BOT_TOKEN");
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasVoice()) {
            Message message = update.getMessage();
            Voice inputVoice = message.getVoice();
            String chatId = message.getChatId().toString();

            // Download the voice message
            String fileId = inputVoice.getFileId();
//            String filePath = downloadVoiceMessage(fileId);

            // Send the voice message back to the user
            try {
                SendVoice voice = new SendVoice();
                InputFile inputFile = new InputFile(fileId);
                voice.setChatId(chatId);
                voice.setVoice(inputFile);
                execute(voice);

                // Send a text message to confirm receipt of voice message
                SendMessage reply = new SendMessage();
                reply.setChatId(chatId);
                reply.setText("Your message: " + inputVoice.getFileId());
                execute(reply);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "YourBotUsername";
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    // Helper method to download voice messages from Telegram servers
    private String downloadVoiceMessage(String fileId) {
        // Replace with your code to download the file
        return "/path/to/your/file";
    }
}