package org.example.service;

import org.example.bot.MyTelegramBot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class ScheduledTaskService
{
    private static final Logger logger = LoggerFactory.getLogger(MyTelegramBot.class);

    @Autowired
    UserService userService;

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    public void checkUserDelayNotifications(){
        List<Long> notifications = userService.getDelayNotifications();
        notifications.forEach(chatId -> new SendMessage(chatId.toString(), "You have new recordings."));
        logger.info("Scheduler: " + notifications);
    }

    @Scheduled(fixedRate = 15, timeUnit = TimeUnit.MINUTES)
    public void checkUserInstantNotifications(){
        List<Long> notifications = userService.getDelayNotifications();
        notifications.forEach(chatId -> new SendMessage(chatId.toString(), "You have new recordings."));
        logger.info("Scheduler: " + notifications);
    }
}
