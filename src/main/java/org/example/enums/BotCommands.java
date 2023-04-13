package org.example.enums;

public enum BotCommands
{

    START("/start", "Register yourself"),
    HELP("/help", "Get help"),
    END("/end", "Stop using bot"),
    TUTORIAL("/tutorial", "Get a tutorial");

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