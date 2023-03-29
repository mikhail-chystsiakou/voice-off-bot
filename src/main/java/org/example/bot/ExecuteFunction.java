package org.example.bot;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.Serializable;

@FunctionalInterface
public interface ExecuteFunction {
    <T extends Serializable, Method extends BotApiMethod<T>> T execute(Method method) throws TelegramApiException;
}
