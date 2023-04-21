package org.example.enums;

import org.example.config.BotConfig;

public enum MessageType {
    DATA,
    REPLY,
    FEEDBACK;

    private static BotConfig botConfig;

    public static void setConfig(BotConfig bc) {
        botConfig = bc;
    }

    public String getDir() {
        return switch (this) {
            case DATA -> botConfig.getVoicesPath();
            case REPLY -> botConfig.getRepliesPath();
            case FEEDBACK -> botConfig.getFeedbacksPath();
        };
    }
}
