package org.example.bot;

import lombok.SneakyThrows;
import org.example.config.BotConfig;
import org.example.enums.BotCommands;
import org.example.service.ButtonsService;
import org.example.service.UpdateHandler;
import org.example.service.UserService;
import org.example.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
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

        List<BotCommand> botCommands = Arrays.stream(BotCommands.values())
                .map(co -> new BotCommand(co.getCommand(), co.getDescription()))
                .collect(Collectors.toList());
        execute(new SetMyCommands(botCommands, new BotCommandScopeDefault(), null));
    }


    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            if (!isRegistered(message.getFrom().getId())
                && message.hasText()
                && !message.getText().startsWith(BotCommands.START.getCommand())) {
                    updateHandler.userNotRegistered(message);
            }else if (message.hasVoice()){
                updateHandler.handleVoiceMessage(message);
            }else if (message.hasText()){
                String inputMessage = message.getText();
                if (inputMessage.startsWith(BotCommands.START.getCommand())){
                    updateHandler.registerUser(message);
                } else if (BotCommands.PULL.getCommand().equals(inputMessage) || BotCommands.PULL.getDescription().equals(inputMessage)){
                    updateHandler.pull(message);
                } else if (BotCommands.FOLLOWERS.getCommand().equals(inputMessage)){
                    updateHandler.getFollowers(message);
                } else if (BotCommands.SUBSCRIPTIONS.getCommand().equals(inputMessage)){
                    updateHandler.getSubscriptions(message);
                } else if (inputMessage.startsWith(BotCommands.UNSUBSCRIBE.getCommand())) {
                    updateHandler.unsubscribe(message);
                } else if (inputMessage.startsWith(BotCommands.REMOVE_FOLLOWER.getCommand())) {
                    updateHandler.removeFollower(message);
                } else if (BotCommands.END.getCommand().equals(inputMessage)) {
                    updateHandler.end(message);
                } else if (inputMessage.startsWith(BotCommands.HELP.getCommand())) {
                    updateHandler.help(message);
                } else {
                    updateHandler.unsupportedResponse(message);
                }
            }
            if (message.getUserShared() != null){
                updateHandler.handleContact(message);
            }
        }
        if (update.hasCallbackQuery()){
            updateHandler.handleConfirmation(update.getCallbackQuery());
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