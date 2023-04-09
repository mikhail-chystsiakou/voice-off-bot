package org.example.enums;

public enum ButtonCommands
{
    PULL("/pull", "Get updates"),
    MANAGE_SUBSCRIPTIONS("/subscriptions", "Manage Subscriptions"),
    REMOVE_SUBSCRIBER("/removefollower", "Unsubscribe user"),
    UNSUBSCRIBE("/unsubscribe", "Unfollow user"),
    SUBSCRIBE("/subscribe", "Subscribe to user"),
    REMOVE_RECORDING("/removerecording", "Remove recording"),
    RETURN_TO_MAIN_MENU("/return", "Return to main menu");

    String command;
    String description;

    ButtonCommands(String command, String description){
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