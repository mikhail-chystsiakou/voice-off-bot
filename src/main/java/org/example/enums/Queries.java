package org.example.enums;

public enum Queries
{
    GET_USER_BY_ID("SELECT user_id FROM users WHERE user_id = ?"),
    ADD_USER("INSERT INTO users(user_id, username, first_name, last_name, chat_id, registered_date, notifications) VALUES(?, ?, ?, ?, ?, current_timestamp, 0) ON CONFLICT DO NOTHING"),
    ADD_CONTACT("INSERT INTO user_subscriptions(user_id, followee_id, last_pull_timestamp) VALUES(?, ?, current_timestamp) ON CONFLICT DO NOTHING"),
    GET_LATEST_FOLLOW_REQUEST_TIMESTAMP("SELECT latest_request_timestamp FROM follow_requests WHERE user_id = ? and followee_id = ?"),
    CHECK_FOLLOWING("SELECT COUNT(*) FROM user_subscriptions WHERE user_id = ? and followee_id = ?"),
    GET_FOLLOWEE_ID_BY_PULL_MESSAGE_ID("select followee_id from pull_messages where pull_message_id = ?"),
    GET_CHAT_ID_BY_USER_ID("SELECT chat_id FROM users where user_id = ?"),
    ADD_REQUEST_TO_CONFIRM("INSERT INTO follow_requests(user_id, followee_id) VALUES(?, ?) ON CONFLICT DO NOTHING"),
    GET_USER_ID_BY_FOLLOWEE_ID("SELECT user_id FROM follow_requests where followee_id = ?"),
    REMOVE_REQUEST_TO_CONFIRM("DELETE FROM follow_requests where user_id = ? and followee_id = ?"),
    // userId, fileId, duration, sqlTimestamp, messageId, fileSize, replyToMessageId
    ADD_AUDIO("INSERT INTO user_audios(user_id, file_id, duration, " +
            "recording_timestamp, message_id, file_size, reply_to_message_id) " +
            "VALUES(?, ?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING"),
    GET_LAST_PULL_TIME("SELECT followee_id user_id, last_pull_timestamp\n" +
            "FROM user_subscriptions us, user_audios ua left join user_feedbacks uf on uf.message_id = ua.message_id\n" +
            "WHERE ua.user_id = us.followee_id\n" +
            " and ua.recording_timestamp > us.last_pull_timestamp\n" +
            " and ua.reply_to_message_id is null\n" +
            "  and us.user_id = ? \n" +
            "  and uf.user_id is null \n" +
            "group by us.user_id, us.followee_id, us.last_pull_timestamp\n" +
            "having count(1) >= 1\n" +
            "limit 1"),
    GET_VOICE_PARTS("select recording_timestamp, duration, description, pull_count, message_id\n" +
            "from user_audios\n" +
            "where user_id = ?\n" +
            "  and recording_timestamp > ?\n" +
            "  and recording_timestamp <= ?\n" +
            " order by recording_timestamp"),
    SET_PULL_TIMESTAMP("UPDATE user_subscriptions set last_pull_timestamp = ? where user_id = ? and followee_id = ?"),
    REMOVE_REPLY_PULL_TIMESTAMP("delete from user_replies where user_id = ? and subscriber_id = ?"),
    ADD_REPLY_PULL_TIMESTAMP("insert into user_replies(last_pull_timestamp, user_id, subscriber_id) values(?, ?, ?)"),
    SET_PULL_COUNT("UPDATE user_audios set pull_count = ? where user_id = ? and message_id = ?"),
    SET_OK_MESSAGE_ID("UPDATE user_audios set ok_message_id = ? where user_id = ? and message_id = ?"),
    ADD_PULL_MESSAGE_ID("insert into pull_messages (followee_id, pull_message_id, orig_message_ids) values (?, ?, ?)"),
    GET_OK_MESSAGE_ID("select ok_message_id from user_audios where user_id = ? and message_id = ?"),
    REMOVE_USER("delete from users where user_id = ?"),
    GET_USER_ID_BY_ID("select * from users where user_id = ?"),
    REMOVE_LAST_USER_RECORD("delete from user_audios where user_id = ? and message_id = ?"),
    GET_USER_NAMES_BY_USER_ID("select username, first_name, last_name from users where user_id = ?"),
    UPDATE_MESSAGE_DESCRIPTION("update user_audios set description = ? where user_id = ? and message_id = ?"),
    UPDATE_TIMEZONE("update users set time_zone = ? where user_id = ?"),
    GET_PREVIOUS_PULL_TIMESTAMP("select followee_id, pull_timestamp, last_pull_timestamp from pull_stats\n" +
            "         where pull_timestamp <= ?\n" +
            "         and user_id = ?\n" +
            "order by last_pull_timestamp desc limit 1"),
    GET_VOICE_PARTS_BY_TIMESTAMPS("" +
            "select * from user_audios where user_id = ? " +
            "and recording_timestamp > ? and recording_timestamp < ?" +
            "order by recording_timestamp asc"),
    UPDATE_NOTIFICATION_BY_USER("update users set notifications = ? where user_id = ?"),
    UPDATE_FEEDBACK_ALLOWED_BY_USER("update users set feedback_mode_allowed = ? where user_id = ?"),
    UPDATE_FEEDBACK_ENABLED_BY_USER("update users set feedback_mode_enabled = ? where user_id = ?"),
    UPDATE_REPLY_ENABLED_BY_USER("update users set reply_mode_followee_id = ?, reply_mode_message_id = ? where user_id = ?"),
    GET_USERS_FOR_DELAY_NOTIFICATIONS("select u.user_id, u.time_zone from users u, user_subscriptions s where s.followee_id = ? and s.user_id = u.user_id and u.notifications = 2"),
    GET_USERS_FOR_INSTANT_NOTIFICATIONS("select u.user_id from users u, user_subscriptions s where s.followee_id = ? and s.user_id = u.user_id and u.notifications = 1"),
    GET_CHAT_ID_FOR_NOTIFICATIONS("select u.chat_id from users_notifications n, users u where n.estimated_time < current_time and n.user_id = u.user_id"),
    DELETE_USER_FROM_DELAY_NOTIFICATIONS("delete from users_notifications where user_id = ?"),
    CHECK_USER_NOTIFICATION("select count(*) from users_notifications where user_id = ?"),
    ADD_USER_NOTIFICATION("insert into users_notifications(user_id, estimated_time) values(?, ?) ON CONFLICT DO NOTHING"),
    GET_CHAT_ID_FOR_DELAY_NOTIFICATIONS("select u.chat_id from users_delay_notifications n, users u where n.estimated_time < current_time and n.user_id = u.user_id"),
    STORE_FEEDBACK("insert into user_feedbacks (user_id, message_id, text, file_id) values (?, ?, ?, ?)"),
    GET_REPLIES("select *\n" +
            "from\n" +
            "    user_audios ua,\n" +
            "    (select *\n" +
            "        from user_replies ur, user_audios ua\n" +
            "    where\n" +
            "        ur.user_id = ?\n" +
            "      and ur.subscriber_id = ua.user_id\n" +
            "      and ur.user_message_id = ua.reply_to_message_id\n" +
            "      and ua.recording_timestamp >= ur.last_pull_timestamp\n" +
            "      \n" +
            "    group by ua.user_id\n" +
            "    limit 1) r\n" +
            "where ua.message_id = r.message_id"),
    GET_FILE_ID_BY_USER_AND_MESSAGE_ID("select file_id, duration, recording_timestamp from user_audios where user_id = ? and message_id = ?"),
    // returns
    GET_REPLY_LAST_PULL_TIME("" +
            "SELECT us.user_id user_id,\n" +
            "       min(coalesce(ur.last_pull_timestamp, to_timestamp(0))) last_pull_timestamp\n" +
            "FROM\n" +
            "    user_subscriptions us\n" +
            "    join pull_messages pm on pm.followee_id = us.followee_id\n" +
            "    join user_audios ua_subscriber\n" +
            "        on ua_subscriber.user_id = us.user_id\n" +
            "        and ua_subscriber.reply_to_message_id = pm.pull_message_id\n" +
            "    left join user_replies ur\n" +
            "          on ur.user_id = us.followee_id\n" +
            "          and ur.subscriber_id = us.user_id\n" +
            "    left join user_feedbacks uf on uf.message_id = ua_subscriber.message_id\n" +
            "WHERE\n" +
            "   us.followee_id = ?\n" +
            "   and (\n" +
            "       ua_subscriber.recording_timestamp > ur.last_pull_timestamp \n" +
            "           or ur.last_pull_timestamp is null\n" +
            "   )\n" +
            "   and uf.user_id is null\n" +
            "group by us.user_id, ur.last_pull_timestamp\n" +
            "having count(1) >= 1\n" +
            "limit 1"),
    DELETE_NOTIFICATIONS("delete from users_notifications where estimated_time < current_time");

    String value;

    Queries(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}
