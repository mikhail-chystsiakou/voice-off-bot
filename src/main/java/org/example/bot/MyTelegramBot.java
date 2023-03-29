package org.example.bot;

import lombok.SneakyThrows;
import org.example.config.BotConfig;
import org.example.service.UpdateHandler;
import org.example.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.List;

@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    private static String BOT_TOKEN;
    UpdateHandler updateHandler;

    @Autowired
    public MyTelegramBot(BotConfig botConfig, UpdateHandler updateHandler) throws TelegramApiException
    {
        BOT_TOKEN = botConfig.getToken();
        this.updateHandler = updateHandler;
        execute(new SetMyCommands(Arrays.asList(new BotCommand("/pull", "pull")), new BotCommandScopeDefault(), null));
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (message.hasVoice()){
                SendAudio reply = updateHandler.handleVoiceMessage(message);
                execute(reply);
            }
            if (message.hasText()){
                Pair<SendMessage, List<SendVoice>> reply = updateHandler.handleText(message);
                if (reply.getKey() == null){
                    if (reply.getValue().isEmpty()) {
                        SendMessage sm = new SendMessage();
                        sm.setText("No updates yet");
                        sm.setChatId(message.getChatId());
                        execute(sm);
                    } else {
                        reply.getValue().stream().forEach(record -> {
                            try
                            {
                                execute(record);
                            }
                            catch (TelegramApiException e)
                            {
                                e.printStackTrace();
                            }
                        });
                    }
                }
                else {
                    execute(reply.getKey());
                }
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