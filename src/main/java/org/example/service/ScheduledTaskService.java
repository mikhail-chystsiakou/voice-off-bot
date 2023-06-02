package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.model.AnnouncementInfo;
import org.example.service.impl.UserServiceImpl;
import org.example.util.ExecuteFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class ScheduledTaskService
{

    @Autowired
    UserServiceImpl userService;

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
        log.info("Scheduler: " + notifications);
    }

    private void checkAnnouncements()
    {
        List<AnnouncementInfo> announcements = announcementsService.getAnnouncements();

        log.info("announcements: " + announcements);

        announcements.forEach(a -> {
            announcementsService.runAnnouncementProcess(a);
            announcementsService.setPassedForAnnouncement(a.getId());
        });

    }
}
