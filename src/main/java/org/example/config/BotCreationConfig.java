package org.example.config;

import org.example.bot.MyTelegramBot;
import org.example.service.UpdateHandler;
import org.example.service.UserService;
import org.example.util.ExecuteFunction;
import org.example.util.SendAudioFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaBotMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.concurrent.ThreadPoolExecutor;

@Configuration
public class BotCreationConfig {
    @Autowired
    BotConfig botConfig;
    @Autowired
    UpdateHandler updateHandler;
    @Autowired
    UserService userService;
    @Autowired
    TaskExecutor taskExecutor;

    @Bean
    public MyTelegramBot bot(BotConfig botConfig,
                             UpdateHandler updateHandler,
                             UserService userService,
                             TaskExecutor taskExecutor) {
        DefaultBotOptions botOptions = new DefaultBotOptions();
        botOptions.setBaseUrl(botConfig.apiUrl);
        MyTelegramBot bot = null;
        try {
            bot = new MyTelegramBot(botOptions, botConfig, updateHandler, userService, taskExecutor);
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
}
