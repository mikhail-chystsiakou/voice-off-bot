package org.example.enums;

public enum ButtonCommands
{
    PULL("/pull", "Pull \uD83E\uDEF4"),
    MANAGE_SUBSCRIPTIONS("/subscriptions", "Subscriptions \uD83D\uDC68\u200D\uD83D\uDC69\u200D\uD83D\uDC67\u200D\uD83D\uDC66"),
    REMOVE_SUBSCRIBER("/removefollower", "Remove subscriber \uD83E\uDD7A"),
    UNSUBSCRIBE("/unsubscribe", "Unsubscribe from \uD83D\uDEB6\u200D♂"),
    SUBSCRIBE("/subscribe", "Subscribe to \uD83C\uDF7B"),
    REMOVE_RECORDING("/removerecording", "Remove this recording \uD83D\uDDD1"),
    ENABLE_FEEDBACK_MODE("/feedback", "Enable Feedback Mode \uD83D\uDE4B\u200D♀️"),
    DISABLE_FEEDBACK_MODE("/feedback", "Disable Feedback Mode \uD83D\uDE4A"),
    RETURN_TO_MAIN_MENU("/return", "Previous menu \uD83D\uDD19"),
    FOLLOWERS("/followers", "Followers \uD83D\uDC42"),
    FOLLOWING("/following", "Following \uD83D\uDC44")
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