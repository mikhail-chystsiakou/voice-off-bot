package org.example.util;

import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface SendImgFunction {
    Message execute(SendPhoto sendPhoto) throws TelegramApiException;
}
