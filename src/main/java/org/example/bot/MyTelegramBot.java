package org.example.bot;

import lombok.SneakyThrows;
import org.example.config.BotConfig;
import org.example.enums.BotCommands;
import org.example.enums.ButtonCommands;
import org.example.service.UpdateHandler;
import org.example.service.UserService;
import org.example.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetUserProfilePhotos;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.*;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaAudio;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaDocument;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

//@Component
public class MyTelegramBot extends TelegramLongPollingBot {

    private final String BOT_TOKEN;
    UpdateHandler updateHandler;
    UserService userService;
    TaskExecutor taskExecutor;

    public MyTelegramBot(DefaultBotOptions botOptions,
                         BotConfig botConfig,
                         UpdateHandler updateHandler,
                         UserService userService,
                         TaskExecutor taskExecutor) throws TelegramApiException
    {
        super(botOptions, botConfig.getToken());
        BOT_TOKEN = botConfig.getToken();
        this.updateHandler = updateHandler;
        this.userService = userService;
        this.taskExecutor = taskExecutor;

        List<BotCommand> botCommands = Arrays.stream(BotCommands.values())
                .map(co -> new BotCommand(co.getCommand(), co.getDescription()))
                .collect(Collectors.toList());
        execute(new SetMyCommands(botCommands, new BotCommandScopeDefault(), null));
    }


    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        System.out.println("got message");
        taskExecutor.execute(() -> {
            try {
                handleUpdate(update);
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        });

    }

    private void handleUpdate(Update update) throws TelegramApiException {
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
                } else if (ButtonCommands.PULL.getCommand().equals(inputMessage) || ButtonCommands.PULL.getDescription().equals(inputMessage)){
                    updateHandler.pull(message);
                } else if (ButtonCommands.MANAGE_SUBSCRIPTIONS.getCommand().equals(inputMessage) || ButtonCommands.MANAGE_SUBSCRIPTIONS.getDescription().equals(inputMessage)){
                    updateHandler.getManageSubscriptionsMenu(message);
//                    updateHandler.getSubscriptions(message);
                } else if (inputMessage.startsWith(ButtonCommands.UNSUBSCRIBE.getCommand())) {
                    updateHandler.unsubscribe(message);
                } else if (inputMessage.startsWith(ButtonCommands.REMOVE_SUBSCRIBER.getCommand())) {
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
                String requestId = message.getUserShared().getRequestId();
                if ("1".equals(requestId))
                {
                    updateHandler.handleContact(message);
                }
                if ("2".equals(requestId))
                {
                    updateHandler.unsubscribe(message);
                }
                if ("3".equals(requestId))
                {
                    updateHandler.removeFollower(message);
                }
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

    public void onRegister() {
        System.out.println("bot registered");
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