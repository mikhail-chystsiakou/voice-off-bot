package org.example.enums;

public enum ButtonCommands
{
    PULL("/pull", "Get updates"),
    MANAGE_SUBSCRIPTIONS("/subscriptions", "Manage Subscriptions"),
    REMOVE_SUBSCRIBER("/removefollower", "Remove follower"),
    UNSUBSCRIBE("/unsubscribe", "Unfollow user"),
    SUBSCRIBE("/subscribe", "Follow user"),
    REMOVE_RECORDING("/removerecording", "Remove this recording"),
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