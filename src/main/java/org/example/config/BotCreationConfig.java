package org.example.config;

import org.example.bot.MyTelegramBot;
import org.example.service.UpdateHandler;
import org.example.service.impl.UserServiceImpl;
import org.example.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Configuration
public class BotCreationConfig {
    @Autowired
    BotConfig botConfig;
    @Autowired
    UpdateHandler updateHandler;
    @Autowired
    UserServiceImpl userService;
    @Autowired
    TaskExecutor taskExecutor;
    @Autowired
    PullProcessingSet pullProcessingSet;
    @Autowired
    ThreadLocalMap tlm;

    @Bean
    public MyTelegramBot bot(BotConfig botConfig,
                             UpdateHandler updateHandler,
                             UserServiceImpl userService,
                             TaskExecutor taskExecutor) {
        DefaultBotOptions botOptions = new DefaultBotOptions();
        botOptions.setBaseUrl(botConfig.apiUrl);
        MyTelegramBot bot = null;
        try {
            bot = new MyTelegramBot(botOptions, botConfig, updateHandler, userService, taskExecutor, pullProcessingSet, tlm);
            System.out.println("Bot connected to api: " + botConfig.apiUrl);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        return bot;
    }

//    @Bean
//    public TaskExecutor taskExecutor() {
//
//    }

    @Bean
    ExecuteFunction extractExecuteFunction(MyTelegramBot bot) {
        return bot::execute;
    }

    @Bean
    SendAudioFunction extractSendAudioFunction(MyTelegramBot bot) {
        return bot::execute;
    }

    @Bean
    SendVideoFunction extractSendVideoFunction(MyTelegramBot bot) {
        return bot::execute;
    }
    @Bean
    SendImgFunction extractSendImgFunction(MyTelegramBot bot) {
        return bot::execute;
    }
}
