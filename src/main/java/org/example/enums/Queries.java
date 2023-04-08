package org.example.enums;

public enum Queries
{
    GET_USER_BY_ID("SELECT user_id FROM users WHERE user_id = ?"),
    ADD_USER("INSERT INTO users(user_id, user_name, chat_id, registered_date) VALUES(?, ?, ?, current_timestamp) ON CONFLICT DO NOTHING"),
    ADD_CONTACT("INSERT INTO user_subscriptions(user_id, followee_id, last_pull_timestamp) VALUES(?, ?, current_timestamp) ON CONFLICT DO NOTHING"),
    CHECK_FOLLOWEE("SELECT COUNT(*) FROM follow_requests WHERE user_id = ? and followee_id = ?"),
    CHECK_FOLLOWING("SELECT COUNT(*) FROM user_subscriptions WHERE user_id = ? and followee_id = ?"),
    GET_CHAT_ID_BY_USER_ID("SELECT chat_id FROM users where user_id = ?"),
    ADD_REQUEST_TO_CONFIRM("INSERT INTO follow_requests(user_id, followee_id) VALUES(?, ?) ON CONFLICT DO NOTHING"),
    GET_USER_ID_BY_FOLLOWEE_ID("SELECT user_id FROM follow_requests where followee_id = ?"),
    REMOVE_REQUEST_TO_CONFIRM("DELETE FROM follow_requests where user_id = ? and followee_id = ?"),
    ADD_AUDIO("INSERT INTO user_audios(user_id, file_id, duration, recording_timestamp) VALUES(?, ?, ?, ?) ON CONFLICT DO NOTHING"),
    PULL_RECORDS_BY_USER_ID("SELECT u.user_name, ua.file_id, ua.recording_timestamp\n" +
            "FROM user_audios ua, user_subscriptions us, users u\n" +
            "WHERE ua.user_id = us.followee_id\n" +
            "  and ua.recording_timestamp > us.last_pull_timestamp\n" +
            "  and u.user_id = ua.user_id\n" +
            "  and us.user_id = ?"),
    GET_LAST_PULL_TIME("SELECT followee_id, last_pull_timestamp\n" +
            "FROM user_subscriptions us, user_audios ua\n" +
            "WHERE ua.user_id = us.followee_id\n" +
            " and ua.recording_timestamp > us.last_pull_timestamp\n" +
            "  and ua.recording_timestamp <= ?\n" +
            "  and us.user_id = ?\n" +
            "group by us.user_id, us.followee_id, us.last_pull_timestamp\n" +
            "having count(1) >= 1\n" +
            "limit 1"),
    GET_VOICE_PARTS("select to_char(recording_timestamp, 'YYYY.MM.DD') recording_day, sum(duration) sum_duration\n" +
            "from user_audios\n" +
            "where user_id = ?\n" +
            "  and recording_timestamp > ?\n" +
            "  and recording_timestamp <= ?\n" +
            "group by to_char(recording_timestamp, 'YYYY.MM.DD')\n" +
            " order by to_char(recording_timestamp, 'YYYY.MM.DD')"),
    PULL_RECORDS_BY_USER_ID_2("SELECT file_id\n" +
            "FROM user_audios ua, user_subscriptions us\n" +
            "WHERE ua.user_id = us.followee_id\n" +
            "  and ua.recording_timestamp > us.last_pull_timestamp\n" +
            "and us.user_id = ?"),
    SET_PULL_TIMESTAMP("UPDATE user_subscriptions set last_pull_timestamp = ? where user_id = ? and followee_id = ?"),
    GET_USER_ID_BY_NAME("select user_id, chat_id from users where user_name = ?"),
    REMOVE_USER("delete from users where user_id = ?");

    String value;

    Queries(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}
