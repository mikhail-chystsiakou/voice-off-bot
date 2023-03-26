package org.example.enums;

public enum Queries
{
    GET_USER_BY_ID("SELECT user_id FROM users WHERE user_id = ?"),
    ADD_USER("INSERT INTO users(user_id, registered_date, chat_id) VALUES(?, ?, ?) ON CONFLICT DO NOTHING"),
    ADD_CONTACT("INSERT INTO user_subscription(user_id, folowee_id) VALUES(?, ?) ON CONFLICT DO NOTHING"),
    CHECK_FOLOWEE("SELECT COUNT(*) FROM folow_requests WHERE user_id = ? and folowee_id = ?"),
    CHECK_FOLLOWING("SELECT COUNT(*) FROM user_subscription WHERE user_id = ? and folowee_id = ?"),
    GET_CHAT_ID_BY_USER_ID("SELECT chat_id FROM users where user_id = ?"),
    ADD_REQUEST_TO_CONFIRM("INSERT INTO folow_requests(user_id, folowee_id) VALUES(?, ?) ON CONFLICT DO NOTHING"),
    GET_USER_ID_BY_FOLOWEE_ID("SELECT user_id FROM folow_requests where folowee_id = ?");

    String value;

    Queries(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}
