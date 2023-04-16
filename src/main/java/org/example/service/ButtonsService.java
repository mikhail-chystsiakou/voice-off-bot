package org.example.service;

import org.example.Constants;
import org.example.enums.ButtonCommands;
import org.example.model.UserInfo;
import org.example.util.ThreadLocalMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButtonRequestUser;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.example.util.ThreadLocalMap.*;

@Component
public class ButtonsService
{
    private static final Logger logger = LoggerFactory.getLogger(ButtonsService.class);
    @Autowired
    ThreadLocalMap tlm;

    public InlineKeyboardMarkup getInlineKeyboardMarkupForSubscription(long userId){
        InlineKeyboardButton inlineKeyboardButtonYes = new InlineKeyboardButton();
        inlineKeyboardButtonYes.setText("Yes");
        inlineKeyboardButtonYes.setCallbackData("Yes_" + userId);

        InlineKeyboardButton inlineKeyboardButtonNo = new InlineKeyboardButton();
        inlineKeyboardButtonNo.setText("No");
        inlineKeyboardButtonNo.setCallbackData("No_" + userId);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(Arrays.asList(Arrays.asList(inlineKeyboardButtonYes, inlineKeyboardButtonNo)));
        return inlineKeyboardMarkup;
    }

    public ReplyKeyboard getInitMenuButtons()
    {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        KeyboardButton buttonForPull = new KeyboardButton();
        buttonForPull.setText(ButtonCommands.PULL.getDescription());

        KeyboardButton buttonForManagingSubscriptions = new KeyboardButton();
        buttonForManagingSubscriptions.setText(ButtonCommands.MANAGE_SUBSCRIPTIONS.getDescription());

        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(new KeyboardRow(Arrays.asList(buttonForPull, buttonForManagingSubscriptions)));

        UserInfo userInfo = tlm.get(KEY_USER_INFO);
        if (userInfo == null) {
            logger.warn("UserInfo is null!", new RuntimeException());
        }
        if (userInfo != null && userInfo.isFeedbackEnabled()) {
            KeyboardButton feedbackButton = new KeyboardButton();
            feedbackButton.setText(ButtonCommands.SEND_FEEDBACK.getDescription());
            rows.add(new KeyboardRow(Arrays.asList(feedbackButton)));
        }

        keyboardMarkup.setKeyboard(rows);
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
    }

    public ReplyKeyboard getSettingsButtons()
    {
        InlineKeyboardButton timezoneButton = new InlineKeyboardButton();
        timezoneButton.setText("Set timezone");
        timezoneButton.setCallbackData("settings_timezone");

        InlineKeyboardButton notificationsButton = new InlineKeyboardButton();
        notificationsButton.setText("Set up notifications");
        notificationsButton.setCallbackData("settings_notifications");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(Arrays.asList(Arrays.asList(timezoneButton), Arrays.asList(notificationsButton)));
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getShowTimestampsButton()
    {
        InlineKeyboardButton timezoneButton = new InlineKeyboardButton();
        timezoneButton.setText("Show Timestamps");
        timezoneButton.setCallbackData("timestamps_show");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(Arrays.asList(Arrays.asList(timezoneButton)));
        return inlineKeyboardMarkup;
    }


    public InlineKeyboardMarkup getHideTimestampsButton()
    {
        InlineKeyboardButton timezoneButton = new InlineKeyboardButton();
        timezoneButton.setText("Hide Timestamps");
        timezoneButton.setCallbackData("timestamps_hide");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(Arrays.asList(Arrays.asList(timezoneButton)));
        return inlineKeyboardMarkup;
    }

    public ReplyKeyboard getTimezoneMarkup(int stage)
    {
        InlineKeyboardButton timezoneButton = new InlineKeyboardButton();
        timezoneButton.setText("Set timezone");
        timezoneButton.setCallbackData("settings_timezone_" + ++stage);

        InlineKeyboardButton declineTimezoneButton = new InlineKeyboardButton();
        declineTimezoneButton.setText("Skip");
        declineTimezoneButton.setCallbackData("declineTimezone");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(Arrays.asList(Arrays.asList(timezoneButton, declineTimezoneButton)));
        return inlineKeyboardMarkup;
    }

    public ReplyKeyboard getTimezoneMarkup()
    {
        InlineKeyboardButton timezoneButton = new InlineKeyboardButton();
        timezoneButton.setText("Set timezone");
        timezoneButton.setCallbackData("settings_timezone");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(Arrays.asList(Arrays.asList(timezoneButton)));
        return inlineKeyboardMarkup;
    }

    public ReplyKeyboard getManageSubscriptionsMenu()
    {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        KeyboardButtonRequestUser keyboardButtonRequestUser1 = new KeyboardButtonRequestUser();
        keyboardButtonRequestUser1.setRequestId("1");
        keyboardButtonRequestUser1.setUserIsBot(false);

        KeyboardButtonRequestUser keyboardButtonRequestUser2 = new KeyboardButtonRequestUser();
        keyboardButtonRequestUser2.setRequestId("2");
        keyboardButtonRequestUser1.setUserIsBot(false);

        KeyboardButtonRequestUser keyboardButtonRequestUser3 = new KeyboardButtonRequestUser();
        keyboardButtonRequestUser3.setRequestId("3");
        keyboardButtonRequestUser1.setUserIsBot(false);

        KeyboardButton buttonForSubscription = new KeyboardButton();
        buttonForSubscription.setRequestUser(keyboardButtonRequestUser1);
        buttonForSubscription.setText(ButtonCommands.SUBSCRIBE.getDescription());

        KeyboardButton buttonForUnfollowUser = new KeyboardButton();
        buttonForUnfollowUser.setRequestUser(keyboardButtonRequestUser2);
        buttonForUnfollowUser.setText(ButtonCommands.UNSUBSCRIBE.getDescription());

        KeyboardButton buttonForUnsubscribeUser = new KeyboardButton();
        buttonForUnsubscribeUser.setRequestUser(keyboardButtonRequestUser3);
        buttonForUnsubscribeUser.setText(ButtonCommands.REMOVE_SUBSCRIBER.getDescription());

        KeyboardButton buttonReturnToPreviousMenu = new KeyboardButton();
        buttonReturnToPreviousMenu.setText(ButtonCommands.RETURN_TO_MAIN_MENU.getDescription());

        KeyboardButton followers = new KeyboardButton();
        followers.setText(ButtonCommands.FOLLOWERS.getDescription());

        KeyboardButton following = new KeyboardButton();
        following.setText(ButtonCommands.FOLLOWING.getDescription());

        keyboardMarkup.setKeyboard(Arrays.asList(new KeyboardRow(Arrays.asList(buttonForSubscription, buttonForUnfollowUser)),
                                                 new KeyboardRow(Arrays.asList(buttonForUnsubscribeUser, buttonReturnToPreviousMenu)),
                                                 new KeyboardRow(Arrays.asList(followers, following))));
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
    }

    public InlineKeyboardMarkup getButtonForDeletingRecord(Integer messageId){
        InlineKeyboardButton deleteButton = new InlineKeyboardButton();
        deleteButton.setText(ButtonCommands.REMOVE_RECORDING.getDescription());
        deleteButton.setCallbackData("remove_" + messageId.toString());

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(Arrays.asList(Arrays.asList(deleteButton)));
        return inlineKeyboardMarkup;
    }

    public ReplyKeyboard getButtonsForTutorial()
    {
        InlineKeyboardButton approveButton = new InlineKeyboardButton();
        approveButton.setText("Yes");
        approveButton.setCallbackData("isTutorial_1");

        InlineKeyboardButton declineButton = new InlineKeyboardButton();
        declineButton.setText("No");
        declineButton.setCallbackData("declineTutorial");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(Arrays.asList(Arrays.asList(approveButton, declineButton)));
        return inlineKeyboardMarkup;
    }

    public ReplyKeyboard getNextButton(int stage)
    {
        InlineKeyboardButton approveButton = new InlineKeyboardButton();
        approveButton.setText("Next (" + stage + "/4)");
        approveButton.setCallbackData("isTutorial_" + ++stage);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(Arrays.asList(Arrays.asList(approveButton)));
        return inlineKeyboardMarkup;
    }

    public ReplyKeyboard getFinishButton(int stage)
    {
        InlineKeyboardButton finishButton = new InlineKeyboardButton();
        finishButton.setText("Finish");
        finishButton.setCallbackData("isTutorial_" + ++stage);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(Arrays.asList(Arrays.asList(finishButton)));
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup getTimezonesButtons(boolean finish)
    {
        InlineKeyboardButton utc_1200 = new InlineKeyboardButton();
        utc_1200.setText("-12:00");
        utc_1200.setCallbackData("settings_timezone_-1200" + (finish ? "f" : ""));

        InlineKeyboardButton utc_1100 = new InlineKeyboardButton();
        utc_1100.setText("-11:00");
        utc_1100.setCallbackData("settings_timezone_-1100" + (finish ? "f" : ""));

        InlineKeyboardButton utc_1000 = new InlineKeyboardButton();
        utc_1000.setText("-10:00");
        utc_1000.setCallbackData("settings_timezone_-1000" + (finish ? "f" : ""));

        InlineKeyboardButton utc_0930 = new InlineKeyboardButton();
        utc_0930.setText("-09:30");
        utc_0930.setCallbackData("settings_timezone_-0930" + (finish ? "f" : ""));

        InlineKeyboardButton utc_0900 = new InlineKeyboardButton();
        utc_0900.setText("-09:00");
        utc_0900.setCallbackData("settings_timezone_-0900" + (finish ? "f" : ""));

        InlineKeyboardButton utc_0800 = new InlineKeyboardButton();
        utc_0800.setText("-08:00");
        utc_0800.setCallbackData("settings_timezone_-0800" + (finish ? "f" : ""));

        InlineKeyboardButton utc_0700 = new InlineKeyboardButton();
        utc_0700.setText("-07:00");
        utc_0700.setCallbackData("settings_timezone_-0700" + (finish ? "f" : ""));

        InlineKeyboardButton utc_0600 = new InlineKeyboardButton();
        utc_0600.setText("-06:00");
        utc_0600.setCallbackData("settings_timezone_-0600" + (finish ? "f" : ""));

        InlineKeyboardButton utc_0500 = new InlineKeyboardButton();
        utc_0500.setText("-05:00");
        utc_0500.setCallbackData("settings_timezone_-0500" + (finish ? "f" : ""));

        InlineKeyboardButton utc_0400 = new InlineKeyboardButton();
        utc_0400.setText("-04:00");
        utc_0400.setCallbackData("settings_timezone_-0400" + (finish ? "f" : ""));

        InlineKeyboardButton utc_0330 = new InlineKeyboardButton();
        utc_0330.setText("-03:30");
        utc_0330.setCallbackData("settings_timezone_-0330" + (finish ? "f" : ""));

        InlineKeyboardButton utc_0300 = new InlineKeyboardButton();
        utc_0300.setText("-03:00");
        utc_0300.setCallbackData("settings_timezone_-0300" + (finish ? "f" : ""));

        InlineKeyboardButton utc_0200 = new InlineKeyboardButton();
        utc_0200.setText("-02:00");
        utc_0200.setCallbackData("settings_timezone_-0200" + (finish ? "f" : ""));

        InlineKeyboardButton utc_0100 = new InlineKeyboardButton();
        utc_0100.setText("+01:00");
        utc_0100.setCallbackData("settings_timezone_-0100" + (finish ? "f" : ""));

        InlineKeyboardButton utc0000 = new InlineKeyboardButton();
        utc0000.setText("+00:00");
        utc0000.setCallbackData("settings_timezone_0000" + (finish ? "f" : ""));

        InlineKeyboardButton utc0100 = new InlineKeyboardButton();
        utc0100.setText("+01:00");
        utc0100.setCallbackData("settings_timezone_0100" + (finish ? "f" : ""));

        InlineKeyboardButton utc0200 = new InlineKeyboardButton();
        utc0200.setText("+02:00");
        utc0200.setCallbackData("settings_timezone_0200" + (finish ? "f" : ""));

        InlineKeyboardButton utc0300 = new InlineKeyboardButton();
        utc0300.setText("+03:00");
        utc0300.setCallbackData("settings_timezone_0300" + (finish ? "f" : ""));

        InlineKeyboardButton utc0330 = new InlineKeyboardButton();
        utc0330.setText("+03:30");
        utc0330.setCallbackData("settings_timezone_0330" + (finish ? "f" : ""));

        InlineKeyboardButton utc0400 = new InlineKeyboardButton();
        utc0400.setText("+04:00");
        utc0400.setCallbackData("settings_timezone_0400" + (finish ? "f" : ""));

        InlineKeyboardButton utc0430 = new InlineKeyboardButton();
        utc0430.setText("+04:30");
        utc0430.setCallbackData("settings_timezone_0430" + (finish ? "f" : ""));

        InlineKeyboardButton utc0500 = new InlineKeyboardButton();
        utc0500.setText("+05:00");
        utc0500.setCallbackData("settings_timezone_0500" + (finish ? "f" : ""));

        InlineKeyboardButton utc0530 = new InlineKeyboardButton();
        utc0530.setText("+05:30");
        utc0530.setCallbackData("settings_timezone_0530" + (finish ? "f" : ""));

        InlineKeyboardButton utc0545 = new InlineKeyboardButton();
        utc0545.setText("+05:45");
        utc0545.setCallbackData("settings_timezone_0545" + (finish ? "f" : ""));

        InlineKeyboardButton utc0600 = new InlineKeyboardButton();
        utc0600.setText("+06:00");
        utc0600.setCallbackData("settings_timezone_0600" + (finish ? "f" : ""));

        InlineKeyboardButton utc0630 = new InlineKeyboardButton();
        utc0630.setText("+06:30");
        utc0630.setCallbackData("settings_timezone_0630" + (finish ? "f" : ""));

        InlineKeyboardButton utc0700 = new InlineKeyboardButton();
        utc0700.setText("+07:00");
        utc0700.setCallbackData("settings_timezone_0700" + (finish ? "f" : ""));

        InlineKeyboardButton utc0800 = new InlineKeyboardButton();
        utc0800.setText("+08:00");
        utc0800.setCallbackData("settings_timezone_0800" + (finish ? "f" : ""));

        InlineKeyboardButton utc0845 = new InlineKeyboardButton();
        utc0845.setText("+08:45");
        utc0845.setCallbackData("settings_timezone_0845" + (finish ? "f" : ""));

        InlineKeyboardButton utc0900 = new InlineKeyboardButton();
        utc0900.setText("+09:00");
        utc0900.setCallbackData("settings_timezone_0900" + (finish ? "f" : ""));

        InlineKeyboardButton utc0930 = new InlineKeyboardButton();
        utc0930.setText("+09:30");
        utc0930.setCallbackData("settings_timezone_0930" + (finish ? "f" : ""));

        InlineKeyboardButton utc1000 = new InlineKeyboardButton();
        utc1000.setText("+10:00");
        utc1000.setCallbackData("settings_timezone_1000" + (finish ? "f" : ""));

        InlineKeyboardButton utc1030 = new InlineKeyboardButton();
        utc1030.setText("+10:30");
        utc1030.setCallbackData("settings_timezone_1030" + (finish ? "f" : ""));

        InlineKeyboardButton utc1100 = new InlineKeyboardButton();
        utc1100.setText("+11:00");
        utc1100.setCallbackData("settings_timezone_1100" + (finish ? "f" : ""));

        InlineKeyboardButton utc1200 = new InlineKeyboardButton();
        utc1200.setText("+12:00");
        utc1200.setCallbackData("settings_timezone_1200" + (finish ? "f" : ""));

        InlineKeyboardButton utc1245 = new InlineKeyboardButton();
        utc1245.setText("+12:45");
        utc1245.setCallbackData("settings_timezone_1245" + (finish ? "f" : ""));

        InlineKeyboardButton utc1300 = new InlineKeyboardButton();
        utc1300.setText("+13:00");
        utc1300.setCallbackData("settings_timezone_1300" + (finish ? "f" : ""));

        InlineKeyboardButton utc1400 = new InlineKeyboardButton();
        utc1400.setText("+14:00");
        utc1400.setCallbackData("settings_timezone_1400" + (finish ? "f" : ""));

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(Arrays.asList(
                Arrays.asList(utc_1200, utc_1100, utc_1000, utc_0930),
                Arrays.asList(utc_0900, utc_0800, utc_0700, utc_0600),
                Arrays.asList(utc_0500, utc_0400, utc_0330, utc_0300),
                Arrays.asList(utc_0200, utc_0100, utc0000, utc0100),
                Arrays.asList(utc0200, utc0300, utc0330, utc0400),
                Arrays.asList(utc0430, utc0500, utc0530, utc0545),
                Arrays.asList(utc0600, utc0630, utc0700, utc0800),
                Arrays.asList(utc0845, utc0900, utc0930, utc1000),
                Arrays.asList(utc1030, utc1100, utc1200, utc1245),
                Arrays.asList(utc1300, utc1400)
        ));
        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup getNotificationSettingsButtons()
    {
//        InlineKeyboardButton instantNotificationButton = new InlineKeyboardButton();
//        instantNotificationButton.setText("Get instant notifications");
//        instantNotificationButton.setCallbackData(Constants.Settings.SETTING_NOTIFICATIONS_INSTANT);

        InlineKeyboardButton pullNotificationButton = new InlineKeyboardButton();
        pullNotificationButton.setText("Disable");
        pullNotificationButton.setCallbackData(Constants.Settings.SETTING_NOTIFICATIONS_PULL);

        InlineKeyboardButton onceADayNotificationButton = new InlineKeyboardButton();
        onceADayNotificationButton.setText("Delay");
        onceADayNotificationButton.setCallbackData(Constants.Settings.SETTING_NOTIFICATIONS_ONCE_A_DAY);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(Arrays.asList(Arrays.asList(pullNotificationButton),
                                                       Arrays.asList(onceADayNotificationButton)));
        return inlineKeyboardMarkup;
    }
}
