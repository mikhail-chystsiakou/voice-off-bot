package org.example.service;

import org.example.bot.MyTelegramBot;
import org.example.model.AnnouncementInfo;
import org.example.util.ExecuteFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class ScheduledTaskService
{
    private static final Logger logger = LoggerFactory.getLogger(MyTelegramBot.class);

    @Autowired
    UserService userService;

    @Autowired
    AnnouncementsService announcementsService;

    @Autowired
    @Lazy
    ExecuteFunction executeFunction;

    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void run(){
        checkNotifications();
        checkAnnouncements();
    }

    private void checkNotifications()
    {
        List<Long> notifications = userService.getDelayNotifications();
        notifications.forEach(chatId -> {
            try
            {
                executeFunction.execute(new SendMessage(chatId.toString(), "You have new recordings \uD83E\uDEE3"));
            }
            catch (TelegramApiException e)
            {
                e.printStackTrace();
            }
        });
        userService.deleteNotifications();
        logger.info("Scheduler: " + notifications);
    }

    private void checkAnnouncements()
    {
        List<AnnouncementInfo> announcements = announcementsService.getAnnouncements();

        logger.info("announcements: " + announcements);

        announcements.forEach(a -> {
            announcementsService.setPassedForAnnouncement(a.getId());
            announcementsService.runAnnouncementProcess(a);
        });

    }
}
