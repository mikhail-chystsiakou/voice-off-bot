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
}
