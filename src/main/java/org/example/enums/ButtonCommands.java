package org.example.enums;

public enum ButtonCommands
{
    PULL("/pull", "Pull"),
    MANAGE_SUBSCRIPTIONS("/subscriptions", "Subscriptions"),
    REMOVE_SUBSCRIBER("/removefollower", "Remove subscriber"),
    UNSUBSCRIBE("/unsubscribe", "Unsubscribe from"),
    SUBSCRIBE("/subscribe", "Subscribe to"),
    REMOVE_RECORDING("/removerecording", "Remove this recording"),
    SEND_FEEDBACK("/feedback", "Send Feedback"),
    RETURN_TO_MAIN_MENU("/return", "Previous menu"),
    FOLLOWERS("/followers", "Followers"),
    FOLLOWING("/following", "Following")
    ;

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