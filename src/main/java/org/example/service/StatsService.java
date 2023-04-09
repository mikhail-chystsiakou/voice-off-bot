package org.example.service;

import org.example.util.ThreadLocalMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StatsService {
    public static final String PULL_START_KEY = "PULL_START_KEY";
    public static final String PULL_END_KEY = "PULL_END_KEY";
    public static final String PULL_FILE_SIZE_KEY = "PULL_FILE_SIZE_KEY";
    public static final String PULL_USER_ID_KEY = "PULL_USER_ID_KEY";
    public static final String PULL_FOLLOWEE_ID_KEY = "PULL_FOLLOWEE_ID_KEY";
    public static final String PULL_LAST_PULL_TIMESTAMP_KEY = "PULL_LAST_PULL_TIMESTAMP_KEY";
    public static final String PULL_PULL_TIMESTAMP_KEY = "PULL_PULL_TIMESTAMP_KEY";

    private static final String ADD_PULL_STAT =
            "insert into pull_stats(user_id, followee_id, last_pull_timestamp, " +
                    "pull_timestamp, processing_time) " +
                    "values(?, ?, ?, ?, ?)";

    @Autowired
    ThreadLocalMap threadLocalMap;

    public void pullStart() {
        threadLocalMap.put(PULL_START_KEY, String.valueOf(System.currentTimeMillis()));
    }

    public void pullEnd() {
        threadLocalMap.put(PULL_END_KEY, String.valueOf(System.currentTimeMillis()));
    }

    public void storePullStatistics() {
        //todo
    }

    public void setFileSize(long fileSize) {
        threadLocalMap.put(PULL_FILE_SIZE_KEY, String.valueOf(fileSize));
    }

    public void setUserId(long userId) {
        threadLocalMap.put(PULL_USER_ID_KEY, String.valueOf(userId));
    }

    public void setFolloweeId(long followeeId) {
        threadLocalMap.put(PULL_FOLLOWEE_ID_KEY, String.valueOf(followeeId));
    }

    public void setLastPullTimestamp(long lastPullTimestamp) {
        threadLocalMap.put(PULL_LAST_PULL_TIMESTAMP_KEY, String.valueOf(lastPullTimestamp));
    }

    public void setPullTimestamp(long pullTimestamp) {
        threadLocalMap.put(PULL_PULL_TIMESTAMP_KEY, String.valueOf(pullTimestamp));
    }
}
