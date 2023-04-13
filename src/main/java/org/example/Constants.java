package org.example;

public class Constants
{
    public static final String YES = "Yes";
    public static final String No = "No";
    public static final String YOU_WAS_ADDED_TO_THE_SYSTEM = "You was added to the system. Subscribe to other person by clicking the button 'Subscribe to'.";
    public static final String YOU_HAVE_ALREADY_REGISTERED = "You have already registered";

    public interface Messages {
        String SEND_START_FIRST = "Send /start command before using the bot";
        String CHOOSE_OPTION_FROM_MENU = "Choose the option from menu below";
        String OK_RECORDED = "Ok, recorded";
        String OK_REMOVED = "Ok, removed";
        String RECORDING_NOT_FOUND = "Recording not found";
        String NOT_JOINED = "User hasn't joined BeWired yet";
        String NOT_SUBSCRIBED = "You are not subscribed to {0}";
        String NOT_SUBSCRIBER = "{0} is not your subscriber";
        String SUBSCRIPTION_REVOKED = "{0} revoked your subscription";
        String UNSUBSCRIBED = "You will no longer receive updates from {0}";
        String SUBSCRIBER_REMOVED = "{0} will no longer receive your updates";
        String SUBSCRIBER_UNSUBSCRIBED = "{0} unsubscribed from your";
        String SUBSCRIBE_REQUEST_DECLINED = "{0} declined you";
        String SUBSCRIBE_REQUEST_DECLINE_CONFIRM = "Ok, declined";
        String SUBSCRIBE_REQUEST_ACCEPTED = "{0} accepted you";
        String SUBSCRIBE_REQUEST_ACCEPT_CONFIRM = "Ok, accepted";
        String SUBSCRIBE_REQUEST_SENT = "Ok, request to follow {0} sent";
        String SUBSCRIBE_REQUEST_QUESTION = "{0} wants to subscribe. Do you confirm?";
        String ALREADY_SUBSCRIBED = "You already subscribed to {0}";
    }
}