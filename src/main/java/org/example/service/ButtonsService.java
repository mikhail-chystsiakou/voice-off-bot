package org.example.service;

import org.example.enums.ButtonCommands;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButtonRequestUser;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Arrays;

public class ButtonsService
{
    public static InlineKeyboardMarkup getInlineKeyboardMarkupForSubscription(long userId){
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

    public static ReplyKeyboard getInitMenuButtons()
    {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        KeyboardButton buttonForPull = new KeyboardButton();
        buttonForPull.setText(ButtonCommands.PULL.getDescription());

        KeyboardButton buttonForManagingSubscriptions = new KeyboardButton();
        buttonForManagingSubscriptions.setText(ButtonCommands.MANAGE_SUBSCRIPTIONS.getDescription());

        keyboardMarkup.setKeyboard(Arrays.asList(new KeyboardRow(Arrays.asList(buttonForPull, buttonForManagingSubscriptions))));
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
    }

    public static ReplyKeyboard getSettingsButtons()
    {
        InlineKeyboardButton timezoneButton = new InlineKeyboardButton();
        timezoneButton.setText("Set timezone");
        timezoneButton.setCallbackData("settings_timezone");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(Arrays.asList(Arrays.asList(timezoneButton)));
        return inlineKeyboardMarkup;
    }

    public static ReplyKeyboard getTimezoneMarkup()
    {
        InlineKeyboardButton timezoneButton = new InlineKeyboardButton();
        timezoneButton.setText("Set timezone");
        timezoneButton.setCallbackData("settings_timezone");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(Arrays.asList(Arrays.asList(timezoneButton)));
        return inlineKeyboardMarkup;
    }


    public static ReplyKeyboard getManageSubscriptionsMenu()
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

        keyboardMarkup.setKeyboard(Arrays.asList(new KeyboardRow(Arrays.asList(buttonForSubscription, buttonForUnfollowUser)),
                new KeyboardRow(Arrays.asList(buttonForUnsubscribeUser, buttonReturnToPreviousMenu))));
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
    }

    public static InlineKeyboardMarkup getButtonForDeletingRecord(Integer messageId){
        InlineKeyboardButton deleteButton = new InlineKeyboardButton();
        deleteButton.setText(ButtonCommands.REMOVE_RECORDING.getDescription());
        deleteButton.setCallbackData(messageId.toString());

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(Arrays.asList(Arrays.asList(deleteButton)));
        return inlineKeyboardMarkup;
    }

    public static ReplyKeyboard getButtonsForTutorial()
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

    public static ReplyKeyboard getNextButton(int stage)
    {
        InlineKeyboardButton approveButton = new InlineKeyboardButton();
        approveButton.setText("Next");
        approveButton.setCallbackData("isTutorial_" + ++stage);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(Arrays.asList(Arrays.asList(approveButton)));
        return inlineKeyboardMarkup;
    }

    public static ReplyKeyboard getFinishButton(int stage)
    {
        InlineKeyboardButton finishButton = new InlineKeyboardButton();
        finishButton.setText("Finish");
        finishButton.setCallbackData("isTutorial_" + ++stage);

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(Arrays.asList(Arrays.asList(finishButton)));
        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup getTimezonesButtons()
    {
        InlineKeyboardButton utc_1200 = new InlineKeyboardButton();
        utc_1200.setText("UTC-12:00");
        utc_1200.setCallbackData("settings_timezone_-1200");

        InlineKeyboardButton utc_1100 = new InlineKeyboardButton();
        utc_1100.setText("UTC-11:00");
        utc_1100.setCallbackData("settings_timezone_-1100");

        InlineKeyboardButton utc_1000 = new InlineKeyboardButton();
        utc_1000.setText("UTC-10:00");
        utc_1000.setCallbackData("settings_timezone_-1000");

        InlineKeyboardButton utc_0930 = new InlineKeyboardButton();
        utc_0930.setText("UTC-09:30");
        utc_0930.setCallbackData("settings_timezone_-0930");

        InlineKeyboardButton utc_0900 = new InlineKeyboardButton();
        utc_0900.setText("UTC-09:00");
        utc_0900.setCallbackData("settings_timezone_-0900");

        InlineKeyboardButton utc_0800 = new InlineKeyboardButton();
        utc_0800.setText("UTC-08:00");
        utc_0800.setCallbackData("settings_timezone_-0800");

        InlineKeyboardButton utc_0700 = new InlineKeyboardButton();
        utc_0700.setText("UTC-07:00");
        utc_0700.setCallbackData("settings_timezone_-0700");

        InlineKeyboardButton utc_0600 = new InlineKeyboardButton();
        utc_0600.setText("UTC-06:00");
        utc_0600.setCallbackData("settings_timezone_-0600");

        InlineKeyboardButton utc_0500 = new InlineKeyboardButton();
        utc_0500.setText("UTC-05:00");
        utc_0500.setCallbackData("settings_timezone_-0500");

        InlineKeyboardButton utc_0400 = new InlineKeyboardButton();
        utc_0400.setText("UTC-04:00");
        utc_0400.setCallbackData("settings_timezone_-0400");

        InlineKeyboardButton utc_0330 = new InlineKeyboardButton();
        utc_0330.setText("UTC-03:30");
        utc_0330.setCallbackData("settings_timezone_-0330");

        InlineKeyboardButton utc_0300 = new InlineKeyboardButton();
        utc_0300.setText("UTC-03:00");
        utc_0300.setCallbackData("settings_timezone_-0300");

        InlineKeyboardButton utc_0200 = new InlineKeyboardButton();
        utc_0200.setText("UTC-02:00");
        utc_0200.setCallbackData("settings_timezone_-0200");

        InlineKeyboardButton utc_0100 = new InlineKeyboardButton();
        utc_0100.setText("UTC+01:00");
        utc_0100.setCallbackData("settings_timezone_-0100");

        InlineKeyboardButton utc0000 = new InlineKeyboardButton();
        utc0000.setText("UTC+00:00");
        utc0000.setCallbackData("settings_timezone_0000");

        InlineKeyboardButton utc0100 = new InlineKeyboardButton();
        utc0100.setText("UTC+01:00");
        utc0100.setCallbackData("settings_timezone_0100");

        InlineKeyboardButton utc0200 = new InlineKeyboardButton();
        utc0200.setText("UTC+02:00");
        utc0200.setCallbackData("settings_timezone_0200");

        InlineKeyboardButton utc0300 = new InlineKeyboardButton();
        utc0300.setText("UTC+03:00");
        utc0300.setCallbackData("settings_timezone_0300");

        InlineKeyboardButton utc0330 = new InlineKeyboardButton();
        utc0330.setText("UTC+03:30");
        utc0330.setCallbackData("settings_timezone_0330");

        InlineKeyboardButton utc0400 = new InlineKeyboardButton();
        utc0400.setText("UTC+04:00");
        utc0400.setCallbackData("settings_timezone_0400");

        InlineKeyboardButton utc0430 = new InlineKeyboardButton();
        utc0430.setText("UTC+04:30");
        utc0430.setCallbackData("settings_timezone_0430");

        InlineKeyboardButton utc0500 = new InlineKeyboardButton();
        utc0500.setText("UTC+05:00");
        utc0500.setCallbackData("settings_timezone_0500");

        InlineKeyboardButton utc0530 = new InlineKeyboardButton();
        utc0530.setText("UTC+05:30");
        utc0530.setCallbackData("settings_timezone_0530");

        InlineKeyboardButton utc0545 = new InlineKeyboardButton();
        utc0545.setText("UTC+05:45");
        utc0545.setCallbackData("settings_timezone_0545");

        InlineKeyboardButton utc0600 = new InlineKeyboardButton();
        utc0600.setText("UTC+06:00");
        utc0600.setCallbackData("settings_timezone_0600");

        InlineKeyboardButton utc0630 = new InlineKeyboardButton();
        utc0630.setText("UTC+06:30");
        utc0630.setCallbackData("settings_timezone_0630");

        InlineKeyboardButton utc0700 = new InlineKeyboardButton();
        utc0700.setText("UTC+07:00");
        utc0700.setCallbackData("settings_timezone_0700");

        InlineKeyboardButton utc0800 = new InlineKeyboardButton();
        utc0800.setText("UTC+08:00");
        utc0800.setCallbackData("settings_timezone_0800");

        InlineKeyboardButton utc0845 = new InlineKeyboardButton();
        utc0845.setText("UTC+08:45");
        utc0845.setCallbackData("settings_timezone_0845");

        InlineKeyboardButton utc0900 = new InlineKeyboardButton();
        utc0900.setText("UTC+09:00");
        utc0900.setCallbackData("settings_timezone_0900");

        InlineKeyboardButton utc0930 = new InlineKeyboardButton();
        utc0930.setText("UTC+09:30");
        utc0930.setCallbackData("settings_timezone_0930");

        InlineKeyboardButton utc1000 = new InlineKeyboardButton();
        utc1000.setText("UTC+10:00");
        utc1000.setCallbackData("settings_timezone_1000");

        InlineKeyboardButton utc1030 = new InlineKeyboardButton();
        utc1030.setText("UTC+10:30");
        utc1030.setCallbackData("settings_timezone_1030");

        InlineKeyboardButton utc1100 = new InlineKeyboardButton();
        utc1100.setText("UTC+11:00");
        utc1100.setCallbackData("settings_timezone_1100");

        InlineKeyboardButton utc1200 = new InlineKeyboardButton();
        utc1200.setText("UTC+12:00");
        utc1200.setCallbackData("settings_timezone_1200");

        InlineKeyboardButton utc1245 = new InlineKeyboardButton();
        utc1245.setText("UTC+12:45");
        utc1245.setCallbackData("settings_timezone_1245");

        InlineKeyboardButton utc1300 = new InlineKeyboardButton();
        utc1300.setText("UTC+13:00");
        utc1300.setCallbackData("settings_timezone_1300");

        InlineKeyboardButton utc1400 = new InlineKeyboardButton();
        utc1400.setText("UTC+14:00");
        utc1400.setCallbackData("settings_timezone_1400");

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

}
