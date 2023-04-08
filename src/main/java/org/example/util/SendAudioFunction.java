package org.example.util;

import org.telegram.telegrambots.meta.api.methods.send.SendAudio;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public interface SendAudioFunction {
    Message execute(SendAudio sendAudio) throws TelegramApiException;
}
