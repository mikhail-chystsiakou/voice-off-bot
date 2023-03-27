package org.example.bot;

import org.example.util.Pair;
import lombok.SneakyThrows;
import org.example.config.BotConfig;
import org.example.service.UpdateHandler;
import org.postgresql.core.Tuple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;

@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    private static String BOT_TOKEN;
    UpdateHandler updateHandler;

    @Autowired
    public MyTelegramBot(BotConfig botConfig, UpdateHandler updateHandler) {
        BOT_TOKEN = botConfig.getToken();
        this.updateHandler = updateHandler;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasVoice()){
                SendVoice reply = updateHandler.handleVoiceMessage(message);
                execute(reply);
            }
            if (message.hasText()){
                SendMessage reply = updateHandler.handleText(message);
                execute(reply);
            }
            if (message.hasContact()){
                Pair<SendMessage, SendMessage> reply = updateHandler.handleContact(message);
                execute(reply.getKey());
                if (reply.getValue() != null)
                {
                    execute(reply.getValue());
                }
            }
        }
        if (update.hasCallbackQuery()){
            Pair<SendMessage, SendMessage> sendMessage = updateHandler.handleConfirmation(update.getCallbackQuery());
            execute(sendMessage.getKey());
            execute(sendMessage.getValue());
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