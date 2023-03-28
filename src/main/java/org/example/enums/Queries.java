package org.example.enums;

public enum Queries
{
    GET_USER_BY_ID("SELECT user_id FROM users WHERE user_id = ?"),
    ADD_USER("INSERT INTO users(user_id, chat_id, registered_date) VALUES(?, ?, current_timestamp) ON CONFLICT DO NOTHING"),
    ADD_CONTACT("INSERT INTO user_subscriptions(user_id, followee_id) VALUES(?, ?) ON CONFLICT DO NOTHING"),
    CHECK_FOLLOWEE("SELECT COUNT(*) FROM follow_requests WHERE user_id = ? and followee_id = ?"),
    CHECK_FOLLOWING("SELECT COUNT(*) FROM user_subscriptions WHERE user_id = ? and followee_id = ?"),
    GET_CHAT_ID_BY_USER_ID("SELECT chat_id FROM users where user_id = ?"),
    ADD_REQUEST_TO_CONFIRM("INSERT INTO follow_requests(user_id, followee_id) VALUES(?, ?) ON CONFLICT DO NOTHING"),
    GET_USER_ID_BY_FOLLOWEE_ID("SELECT user_id FROM follow_requests where followee_id = ?"),
    REMOVE_REQUEST_TO_CONFIRM("DELETE FROM follow_requests where user_id = ? and followee_id = ?"),
    ADD_AUDIO("INSERT INTO user_audios(user_id, file_id, recording_timestamp) VALUES(?, ?, current_timestamp) ON CONFLICT DO NOTHING");

    String value;

    Queries(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}
