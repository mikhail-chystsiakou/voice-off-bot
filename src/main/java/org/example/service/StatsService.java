package org.example.service;

import org.example.util.ThreadLocalMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;

import java.sql.*;

@Component
public class StatsService {
    public static final String PULL_STAT_ID_KEY = "PULL_STAT_ID_KEY";
    public static final String PULL_START_KEY = "PULL_START_KEY";
    public static final String PULL_END_KEY = "PULL_END_KEY";
    public static final String PULL_END_BEFORE_UPLOAD_KEY = "PULL_END_BEFORE_UPLOAD_KEY";
    public static final String PULL_FILE_SIZE_KEY = "PULL_FILE_SIZE_KEY";
    public static final String PULL_USER_ID_KEY = "PULL_USER_ID_KEY";
    public static final String PULL_FOLLOWEE_ID_KEY = "PULL_FOLLOWEE_ID_KEY";
    public static final String PULL_LAST_PULL_TIMESTAMP_KEY = "PULL_LAST_PULL_TIMESTAMP_KEY";
    public static final String PULL_PULL_TIMESTAMP_KEY = "PULL_PULL_TIMESTAMP_KEY";

    private static final String ADD_PULL_STAT =
            "update pull_stats set " +
                    "user_id = ?, followee_id = ?, last_pull_timestamp = ?, pull_timestamp = ?, " +
                    "start_timestamp = ?, end_before_upload_timestamp = ?, end_timestamp = ?, " +
                    "processing_time_millis = ?, file_size = ? " +
                    "where pull_stat_id = ?";

    @Autowired
    ThreadLocalMap threadLocalMap;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private void updateField(String fieldName, Object value) {
        checkStatLoggingStarted();

        Long pullStatKey = Long.parseLong(threadLocalMap.get(PULL_STAT_ID_KEY));
        String query = "update pull_stats set " + fieldName + " = ? where pull_stat_id = ?";
        jdbcTemplate.update(query, value, pullStatKey);
    }

    public void init() {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String query = "insert into pull_stats(start_timestamp) values (null)";
        jdbcTemplate.update(
                connection -> connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS),
                keyHolder
        );
        threadLocalMap.put(PULL_STAT_ID_KEY, String.valueOf(keyHolder.getKeys().get("pull_stat_id")));
    }

    public void pullStart() {
        checkStatLoggingStarted();

        long start = System.currentTimeMillis();
        updateField("start_timestamp", new Timestamp(start));
        threadLocalMap.put(PULL_START_KEY, String.valueOf(System.currentTimeMillis()));
    }

    public void pullEndBeforeUpload() {
        checkStatLoggingStarted();

        long end = System.currentTimeMillis();
        updateField("end_before_upload_timestamp", new Timestamp(end));
        threadLocalMap.put(PULL_END_BEFORE_UPLOAD_KEY, String.valueOf(System.currentTimeMillis()));
    }

    public void pullEnd() {
        checkStatLoggingStarted();

        long end = System.currentTimeMillis();
        updateField("end_timestamp", new Timestamp(end));
        threadLocalMap.put(PULL_END_KEY, String.valueOf(System.currentTimeMillis()));
    }

    public void setFileSize(long fileSize) {
        checkStatLoggingStarted();

        updateField("file_size", fileSize);
        threadLocalMap.put(PULL_FILE_SIZE_KEY, String.valueOf(fileSize));
    }

    public void setUserId(long userId) {
        checkStatLoggingStarted();

        updateField("user_id", userId);
        threadLocalMap.put(PULL_USER_ID_KEY, String.valueOf(userId));
    }

    public void setFolloweeId(long followeeId) {
        checkStatLoggingStarted();

        updateField("followee_id", followeeId);
        threadLocalMap.put(PULL_FOLLOWEE_ID_KEY, String.valueOf(followeeId));
    }

    public void setLastPullTimestamp(long lastPullTimestamp) {
        checkStatLoggingStarted();

        updateField("last_pull_timestamp", new Timestamp(lastPullTimestamp));
        threadLocalMap.put(PULL_LAST_PULL_TIMESTAMP_KEY, String.valueOf(lastPullTimestamp));
    }

    public void setPullTimestamp(long pullTimestamp) {
        checkStatLoggingStarted();

        updateField("pull_timestamp", new Timestamp(pullTimestamp));
        threadLocalMap.put(PULL_PULL_TIMESTAMP_KEY, String.valueOf(pullTimestamp));
    }

    public void storePullStatistics() {
        checkStatLoggingStarted();

        Long userId = getLong(PULL_USER_ID_KEY);
        Long followeeId = getLong(PULL_FOLLOWEE_ID_KEY);
        Long lastPullTimestamp = getLong(PULL_LAST_PULL_TIMESTAMP_KEY);
        Long pullTimestamp = getLong(PULL_PULL_TIMESTAMP_KEY);
        Long startTimestamp = getLong(PULL_START_KEY);
        Long endBeforeUploadTimestamp = getLong(PULL_END_BEFORE_UPLOAD_KEY);
        Long endTimestamp = getLong(PULL_END_KEY);
        Long fileSize = getLong(PULL_FILE_SIZE_KEY);
        Long statId = getLong(PULL_STAT_ID_KEY);
        Long duration = (startTimestamp != null && endTimestamp != null) ? endTimestamp - startTimestamp : null;

        System.out.println("adding stats, pull timestamp: " + pullTimestamp);
        System.out.println("adding stats, file size: " + fileSize);
        jdbcTemplate.update(ADD_PULL_STAT,
            userId,
            followeeId,
            lastPullTimestamp != null ? new Timestamp(lastPullTimestamp) : null,
            pullTimestamp != null ? new Timestamp(pullTimestamp) : null,
            startTimestamp != null ? new Timestamp(startTimestamp) : null,
            endBeforeUploadTimestamp != null ? new Timestamp(endBeforeUploadTimestamp) : null,
            endTimestamp != null ? new Timestamp(endTimestamp) : null,
            duration,
            fileSize,
            statId
        );
    }

    private Long getLong(String key) {
        String value = threadLocalMap.get(key);
        return value == null ? null : Long.parseLong(value);
    }

    public void cleanup() {
        threadLocalMap.clear();
    }

    private void checkStatLoggingStarted() {
        if (threadLocalMap.get(PULL_STAT_ID_KEY) == null) {
            throw new IllegalStateException("init() method was not called");
        }
    }
}
