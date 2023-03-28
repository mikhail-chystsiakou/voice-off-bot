package org.example.service;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

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
}
