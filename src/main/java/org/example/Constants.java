package org.example;

public class Constants
{
    public static final String YES = "Yes \uD83D\uDC4D";
    public static final String NO = "No \uD83D\uDC4E";
    public static final String YOU_WAS_ADDED_TO_THE_SYSTEM = "You were added to the system \uD83E\uDD73";
    public static final String YOU_HAVE_ALREADY_REGISTERED = "You have already registered \uD83D\uDE09";

    public interface Messages {
        String SEND_START_FIRST = "Send /start command before using the bot \uD83D\uDC82";
        String CHOOSE_OPTION_FROM_MENU = "Choose the option from menu below \uD83D\uDC47";
        String SELECT_SETTING = "Select setting";
        String OK_RECORDED = "Ok, recorded \uD83D\uDC4D";
        String OK_REMOVED = "Ok, removed \uD83D\uDDD1";
        String RECORDING_NOT_FOUND = "Recording not found \uD83D\uDE25";
        String NOT_JOINED = "User hasn't joined BeWired yet \uD83E\uDD7A";
        String NOT_SUBSCRIBED = "You are not subscribed to {0} \uD83E\uDD14";
        String NOT_SUBSCRIBER = "{0} is not your subscriber \uD83E\uDD14";
        String SUBSCRIPTION_REVOKED = "{0} revoked your subscription \uD83D\uDE13";
        String UNSUBSCRIBED = "You will no longer receive updates from {0} \uD83D\uDE15";
        String SUBSCRIBER_REMOVED = "{0} will no longer receive your updates \uD83D\uDE1F";
        String SUBSCRIBER_UNSUBSCRIBED = "{0} unsubscribed from you \uD83D\uDE13";
        String SUBSCRIBE_REQUEST_DECLINED = "{0} declined you \uD83D\uDE1E";
        String SUBSCRIBE_REQUEST_DECLINE_CONFIRM = "Ok, declined \uD83D\uDC7F";
        String SUBSCRIBE_REQUEST_ACCEPTED = "{0} accepted you \uD83E\uDD73";
        String SUBSCRIBE_REQUEST_ACCEPT_CONFIRM = "Ok, accepted \uD83E\uDD73";
        String SUBSCRIBE_REQUEST_SENT = "Ok, request to follow {0} sent ☺️";
        String SUBSCRIBE_REQUEST_QUESTION = "{0} wants to subscribe \uD83C\uDF7B. Do you confirm?";
        String ALREADY_SUBSCRIBED = "You already subscribed to {0} \uD83E\uDD14";
        String REQUEST_ALREADY_SENT = "Your request has already been sent to this user\nYou can sent request once a day per user \uD83D\uDC82";
    }

    public interface Settings {
        String SETTINGS = "settings";
        String SETTING_TIMEZONE = "settings_timezone";
        String SETTING_FEEDBACK = "settings_feedback";
        String SETTING_FEEDBACK_ALLOWED = "settings_feedback_allowed";
        String SETTING_FEEDBACK_PROHIBITED = "settings_feedback_prohibited";
        String SETTING_NOTIFICATIONS = "settings_notifications";
        String SETTING_NOTIFICATIONS_INSTANT = "settings_notifications_instant";
        String SETTING_NOTIFICATIONS_PULL = "settings_notifications_pull";
        String SETTING_NOTIFICATIONS_ONCE_A_DAY = "settings_notifications_once_a_day";
    }
}