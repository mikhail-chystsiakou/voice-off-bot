package org.example.service;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButtonRequestUser;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.api.objects.webapp.WebAppInfo;

import java.util.Arrays;

public class ButtonsService
{
    public static InlineKeyboardMarkup getInlineKeyboardMarkupForSubscription(){
        InlineKeyboardButton inlineKeyboardButtonYes = new InlineKeyboardButton();
        inlineKeyboardButtonYes.setText("Yes");
        inlineKeyboardButtonYes.setCallbackData("Yes");

        InlineKeyboardButton inlineKeyboardButtonNo = new InlineKeyboardButton();
        inlineKeyboardButtonNo.setText("No");
        inlineKeyboardButtonNo.setCallbackData("No");

        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        inlineKeyboardMarkup.setKeyboard(Arrays.asList(Arrays.asList(inlineKeyboardButtonYes, inlineKeyboardButtonNo)));
        return inlineKeyboardMarkup;
    }

    public static ReplyKeyboard getMenuButtons()
    {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        KeyboardButtonRequestUser keyboardButtonRequestUser = new KeyboardButtonRequestUser();
        keyboardButtonRequestUser.setRequestId("2147483646");

        KeyboardButton buttonForSubscription = new KeyboardButton();
        buttonForSubscription.setRequestUser(keyboardButtonRequestUser);
        buttonForSubscription.setText("Subscribe to user");

        KeyboardButton buttonForPull = new KeyboardButton();
        buttonForPull.setText("Get updates");

        keyboardMarkup.setKeyboard(Arrays.asList(new KeyboardRow(Arrays.asList(buttonForSubscription)), new KeyboardRow(Arrays.asList(buttonForPull))));
        keyboardMarkup.setResizeKeyboard(true);
        return keyboardMarkup;
    }
}
