package org.example.enums;

public enum BotCommands
{

    START("/start", "Register yourself"),
    SETTINGS("/settings", "Settings"),
//    HELP("/help", "Get help"),
    TUTORIAL("/tutorial", "Get a tutorial"),
    END("/end", "Stop using bot");

    String command;
    String description;

    BotCommands(String command, String description){
        this.command = command;
        this.description = description;
    }

    public String getCommand(){
        return command;
    }

    public String getDescription() {
        return description;
    }
}