package org.example.enums;

public enum FollowQueries {
    GET_FOLLOWERS(""),
    GET_FOLLOWEES(""),
    REVOKE_FOLLOWER(""),
    UNFOLLOW("");

    String value;

    FollowQueries(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}