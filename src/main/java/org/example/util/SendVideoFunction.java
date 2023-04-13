package org.example.util;

import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface SendVideoFunction {
    Message execute(SendVideo sendVideo) throws TelegramApiException;
}
