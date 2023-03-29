package org.example.enums;

public enum CommandOptions
{

    START("/start", "Register youself"),
    PULL("/pull", "Get updates"),
    FOLLOWERS("/followers", "List your followers"),
    REMOVE_FOLLOWER("/removefollower", "Stop sending updates to follower"),
    SUBSCRIPTIONS("/subscriptions", "List your subscriptions"),
    UNSUBSCRIBE("/unsubscribe", "Unsubscribe from followee"),
    END("/end", "Remove all your data except past messages");

    String command;
    String description;

    CommandOptions(String command, String description){
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
