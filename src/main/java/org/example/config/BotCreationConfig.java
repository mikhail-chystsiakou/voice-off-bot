package org.example.config;

import org.example.bot.MyTelegramBot;
import org.example.service.UpdateHandler;
import org.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Configuration
public class BotCreationConfig {
    @Autowired
    BotConfig botConfig;
    @Autowired
    UpdateHandler updateHandler;
    @Autowired
    UserService userService;

    @Bean
    public MyTelegramBot bot(BotConfig botConfig, UpdateHandler updateHandler, UserService userService) {
        DefaultBotOptions botOptions = new DefaultBotOptions();
        botOptions.setBaseUrl(botConfig.apiUrl);
        MyTelegramBot bot = null;
        try {
            bot = new MyTelegramBot(botOptions, botConfig, updateHandler, userService);
            System.out.println("Bot connected to api: " + botConfig.apiUrl);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        return bot;
    }
}
