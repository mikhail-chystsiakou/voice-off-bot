package org.example.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class ThreadLocalMap {
    private static ConcurrentHashMap<Long, ConcurrentHashMap<String, Object>> tmpFiles;

    public static final String VALUE_TRUE = "TRUE";
    public static final String KEY_USER_INFO = "KEY_USER_INFO";
    public static final String KEY_ORIG_MESSAGE_IDS = "KEY_ORIG_MESSAGE_IDS";
    public static final String KEY_FOLLOWEE_ID = "KEY_FOLLOWEE_ID";
    public static final String KEY_REPLY_MESSAGE = "KEY_REPLY_MESSAGE";

    public ThreadLocalMap() {
        tmpFiles = new ConcurrentHashMap<>();
    }

    public void put(String key, Object value) {
        long threadId = Thread.currentThread().getId();
        tmpFiles.compute(threadId, (k, v) -> {
            if (v == null) {
                v = new ConcurrentHashMap<>();
            }
            v.put(key, value);
            return v;
        });
    }

    public <T> T get(String key) {
        long threadId = Thread.currentThread().getId();

        return (T) tmpFiles.getOrDefault(threadId, new ConcurrentHashMap<>()).get(key);
    }

    public void clear() {
        long threadId = Thread.currentThread().getId();
        tmpFiles.remove(threadId);
    }
}

