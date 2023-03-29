package org.example.enums;

public enum FollowQueries {
    GET_FOLLOWERS("select u.user_name from users u, user_subscriptions us\n" +
            "where u.user_id = us.user_id and us.followee_id = ?"),
    GET_SUBSCRIPTIONS("select u.user_name from users u, user_subscriptions us\n" +
            "where u.user_id = us.followee_id and us.user_id = ?"),
    UNSUBSCRIBE("delete from user_subscriptions where user_id = ? and followee_id = ?");

    String value;

    FollowQueries(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}