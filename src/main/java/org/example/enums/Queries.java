package org.example.enums;

public enum Queries
{
    GET_USER_BY_ID("SELECT user_id FROM users WHERE user_id = ?"),
    ADD_USER("INSERT INTO users(user_id, username, first_name, last_name, chat_id, registered_date) VALUES(?, ?, ?, ?, ?, current_timestamp) ON CONFLICT DO NOTHING"),
    ADD_CONTACT("INSERT INTO user_subscriptions(user_id, followee_id, last_pull_timestamp) VALUES(?, ?, current_timestamp) ON CONFLICT DO NOTHING"),
    CHECK_FOLLOWEE("SELECT COUNT(*) FROM follow_requests WHERE user_id = ? and followee_id = ?"),
    CHECK_FOLLOWING("SELECT COUNT(*) FROM user_subscriptions WHERE user_id = ? and followee_id = ?"),
    GET_CHAT_ID_BY_USER_ID("SELECT chat_id FROM users where user_id = ?"),
    ADD_REQUEST_TO_CONFIRM("INSERT INTO follow_requests(user_id, followee_id) VALUES(?, ?) ON CONFLICT DO NOTHING"),
    GET_USER_ID_BY_FOLLOWEE_ID("SELECT user_id FROM follow_requests where followee_id = ?"),
    REMOVE_REQUEST_TO_CONFIRM("DELETE FROM follow_requests where user_id = ? and followee_id = ?"),
    ADD_AUDIO("INSERT INTO user_audios(user_id, file_id, duration, recording_timestamp, message_id, file_size) VALUES(?, ?, ?, ?, ?, ?) ON CONFLICT DO NOTHING"),
    GET_LAST_PULL_TIME("SELECT followee_id, last_pull_timestamp\n" +
            "FROM user_subscriptions us, user_audios ua\n" +
            "WHERE ua.user_id = us.followee_id\n" +
            " and ua.recording_timestamp > us.last_pull_timestamp\n" +
            "  and ua.recording_timestamp <= ?\n" +
            "  and us.user_id = ?\n" +
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
    SET_PULL_COUNT("UPDATE user_audios set pull_count = ? where user_id = ? and message_id = ?"),
    SET_OK_MESSAGE_ID("UPDATE user_audios set ok_message_id = ? where user_id = ? and message_id = ?"),
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
    GET_VOICE_PARTS_BY_TIMESTAMPS("select * from user_audios where user_id = ? and recording_timestamp > ? and recording_timestamp < ?");

    String value;

    Queries(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}
