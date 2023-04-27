package org.example.util.wrappers;

import org.telegram.telegrambots.meta.api.objects.Chat;

public class ChatToStringWrapper {
    private Chat chat;

    public ChatToStringWrapper(Chat chat) {
        this.chat = chat;
    }

    public String toString() {
        if (chat == null) {
            return "null";
        }
        return "";
    }
}
