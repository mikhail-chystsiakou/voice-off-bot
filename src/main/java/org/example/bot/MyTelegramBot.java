package org.example.bot;

import lombok.SneakyThrows;
import org.example.Constants;
import org.example.config.BotConfig;
import org.example.enums.BotCommands;
import org.example.enums.ButtonCommands;
import org.example.model.UserInfo;
import org.example.repository.UserRepository;
import org.example.service.UpdateHandler;
import org.example.service.UserService;
import org.example.util.PullProcessingSet;
import org.example.util.ThreadLocalMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.TaskExecutor;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MyTelegramBot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(MyTelegramBot.class);

    UpdateHandler updateHandler;
    UserService userService;
    TaskExecutor taskExecutor;
    PullProcessingSet pullProcessingSet;
    ThreadLocalMap tlm;

    public MyTelegramBot(DefaultBotOptions botOptions,
                         BotConfig botConfig,
                         UpdateHandler updateHandler,
                         UserService userService,
                         TaskExecutor taskExecutor,
                         PullProcessingSet pullProcessingSet,
                         ThreadLocalMap tlm) throws TelegramApiException
    {
        super(botOptions, botConfig.getToken());
        this.updateHandler = updateHandler;
        this.userService = userService;
        this.taskExecutor = taskExecutor;
        this.pullProcessingSet = pullProcessingSet;
        this.tlm = tlm;

        List<BotCommand> botCommands = Arrays.stream(BotCommands.values())
                .map(co -> new BotCommand(co.getCommand(), co.getDescription()))
                .collect(Collectors.toList());
        execute(new SetMyCommands(botCommands, new BotCommandScopeDefault(), null));
    }


    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        logger.info("Got message: {}", update);
        taskExecutor.execute(() -> {
            try {
                handleUpdate(update);
                tlm.clear();
            } catch (Exception e) {
                try {
                    if (update != null && update.hasMessage() && update.getMessage().getFrom() != null) {
                        pullProcessingSet.finishProcessingForUser(update.getMessage().getFrom().getId());
                    }
                } catch (Exception e1) {
                    logger.error("Failed to clear processing flag for message {}", update, e1);
                }

                logger.error("Top-level exception", e);
            }
        });

    }

    private void handleUpdate(Update update) throws TelegramApiException, IOException {
        logger.trace("Start processing message with id '{}'", update.getUpdateId());
        Message message = null;
        long userId;
        if (update.hasMessage() || update.hasEditedMessage()) {
            message = update.getMessage();
            if (update.hasEditedMessage()) message = update.getEditedMessage();

            if (!checkRegistered(message)) {
                return;
            }
            userId = message.getFrom().getId();
            userService.loadUserInfo(userId);
        } else if (update.hasCallbackQuery()) {
            userId = update.getCallbackQuery().getFrom().getId();
            userService.loadUserInfo(userId);
        }

        if (message != null) {
            if (update.hasEditedMessage()) {
                updateHandler.storeMessageDescription(message, true);
            }
            else if (message.hasVoice()){
                updateHandler.handleVoiceMessage(message);
            }
            else if (message.hasText()){
                String inputMessage = message.getText();
                if (inputMessage.startsWith(BotCommands.START.getCommand())){
                    updateHandler.registerUser(message);
                }
                else if (ButtonCommands.PULL.getCommand().equals(inputMessage) || ButtonCommands.PULL.getDescription().equals(inputMessage)){
                    updateHandler.pull(message);
                }
                else if (ButtonCommands.MANAGE_SUBSCRIPTIONS.getCommand().equals(inputMessage) || ButtonCommands.MANAGE_SUBSCRIPTIONS.getDescription().equals(inputMessage)){
                    updateHandler.getManageSubscriptionsMenu(message);
                }
                else if (inputMessage.startsWith(ButtonCommands.UNSUBSCRIBE.getCommand())) {
                    updateHandler.unsubscribeFrom(message);
                }
                else if (inputMessage.startsWith(ButtonCommands.REMOVE_SUBSCRIBER.getCommand())) {
                    updateHandler.removeSubscriber(message);
                }
                else if (BotCommands.END.getCommand().equals(inputMessage)) {
                    updateHandler.end(message);
                }
                else if (ButtonCommands.RETURN_TO_MAIN_MENU.getDescription().equals(inputMessage)){
                    updateHandler.returnMainMenu(message);
                }
                else if (BotCommands.TUTORIAL.getCommand().equals(inputMessage)) {
                    updateHandler.getTutorial(message.getChatId(), 1);
                }
                else if (BotCommands.SETTINGS.getCommand().equals(inputMessage)) {
                    updateHandler.getSettings(message);
                }
                else if (ButtonCommands.FOLLOWERS.getDescription().equals(inputMessage)) {
                    updateHandler.getFollowers(message);
                }
                else if (ButtonCommands.FOLLOWING.getDescription().equals(inputMessage)) {
                    updateHandler.getSubscriptions(message);
                }
                else {
                    updateHandler.unsupportedResponse(message);
                }
            }
            if (message.getUserShared() != null){
                String requestId = message.getUserShared().getRequestId();
                if ("1".equals(requestId))
                {
                    updateHandler.subscribeTo(message);
                }
                if ("2".equals(requestId))
                {
                    updateHandler.unsubscribeFrom(message);
                }
                if ("3".equals(requestId))
                {
                    updateHandler.removeSubscriber(message);
                }
            }
        }
        if (update.hasCallbackQuery()){
            String answer = update.getCallbackQuery().getData();
            if (answer.startsWith(Constants.YES) || answer.startsWith(Constants.No)){
                updateHandler.handleConfirmation(update.getCallbackQuery());
            }
            if (answer.contains("declineTutorial")){
                updateHandler.declineTutorial(update.getCallbackQuery().getMessage().getChatId());
            }
            if (answer.contains("settings")){
                updateHandler.setSettings(update.getCallbackQuery(), answer);
            }
            if (answer.contains("timestamps")){
                updateHandler.showHideTimestamps(update.getCallbackQuery(), answer);
            }
            if (answer.contains("remove")) {
                updateHandler.removeRecording(update.getCallbackQuery());
            }
            if (answer.contains("isTutorial")){
                updateHandler.getTutorial(update.getCallbackQuery().getMessage().getChatId(), Integer.parseInt(answer.split("_")[1]));
            }
            if (answer.equals("declineTimezone"))            {
                updateHandler.getTutorial(update.getCallbackQuery().getMessage().getChatId(), 6);
            }
        }
        logger.trace("Finish processing message with id '{}'", update.getUpdateId());
    }

    @Override
    public String getBotUsername() {
        return "YourBotUsername";
    }

    public void onRegister() {
        System.out.println("bot registered");
    }

    private boolean checkRegistered(Message message) throws TelegramApiException, IOException {
        if (!isRegistered(message.getFrom().getId())) {
            if (message.hasText() && message.getText().equals(BotCommands.START.getCommand())) {
                updateHandler.registerUser(message);
            } else {
                updateHandler.userNotRegistered(message);
            }
            return false;
        }
        return true;
    }

    private boolean isRegistered(Long userId) {
        return userService.getUserById(userId) != null;
    }
}