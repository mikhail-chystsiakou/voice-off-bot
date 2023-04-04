package org.example.bot;

import lombok.SneakyThrows;
import org.example.config.BotConfig;
import org.example.enums.CommandOptions;
import org.example.service.UpdateHandler;
import org.example.service.UserService;
import org.example.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    private final String BOT_TOKEN;
    UpdateHandler updateHandler;
    UserService userService;

    @Autowired
    public MyTelegramBot(BotConfig botConfig, UpdateHandler updateHandler, UserService userService) throws TelegramApiException
    {
        BOT_TOKEN = botConfig.getToken();
        this.updateHandler = updateHandler;
        this.userService = userService;

        List<BotCommand> botCommands = Arrays.stream(CommandOptions.values())
                .map(co -> new BotCommand(co.getCommand(), co.getDescription()))
                .collect(Collectors.toList());
        execute(new SetMyCommands(botCommands, new BotCommandScopeDefault(), null));
    }


    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (!isRegistered(message.getFrom().getId())) {
                if (!message.hasText() || !message.getText().equals("/start")) {
                    SendMessage notRegisteredMessage = new SendMessage();
                    notRegisteredMessage.setChatId(message.getChatId());
                    notRegisteredMessage.setText("Send /start to register before using the bot");
                    execute(notRegisteredMessage);
                    return;
                }
            }
            if (message.hasVoice()){
                SendMessage reply = updateHandler.handleVoiceMessage(message);
                execute(reply);
            }
            if (message.hasText()){
                if (message.getText().startsWith("/play")) {

                    SendAudio sa = new SendAudio();
                    sa.setAudio(new InputFile("https://mmich.online/nextcloud/index.php/s/bq9d9JZ4YisfNSq/download"));
                    sa.setAudio(new InputFile("https://bewired.app?from=02.03.2022&to=04.04.2022"));
                    sa.setChatId(message.getChatId());
                    execute(sa);
                    return;
                }
                Pair<SendMessage, List<SendVoice>> reply = updateHandler.handleText(message, this::execute);
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
            if (sendMessage.getKey() != null) {
                execute(sendMessage.getKey());
            }
            if (sendMessage.getValue() != null) {
                execute(sendMessage.getValue());
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

    private boolean isRegistered(Long userId) {
        return userService.getUserById(userId) != null;
    }
}