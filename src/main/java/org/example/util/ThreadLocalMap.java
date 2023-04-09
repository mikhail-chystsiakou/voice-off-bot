package org.example.util;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class ThreadLocalMap {
    private static ConcurrentHashMap<Long, ConcurrentHashMap<String, String>> tmpFiles;

    public ThreadLocalMap() {
        tmpFiles = new ConcurrentHashMap<>();
    }

    public void put(String key, String value) {
        long threadId = Thread.currentThread().getId();
        tmpFiles.compute(threadId, (k, v) -> {
            if (v == null) {
                v = new ConcurrentHashMap<>();
            }
            v.put(key, value);
            return v;
        });
    }

    public String get(String key) {
        long threadId = Thread.currentThread().getId();

        return tmpFiles.getOrDefault(threadId, new ConcurrentHashMap<>()).get(key);
    }

    public void clear() {
        long threadId = Thread.currentThread().getId();
        tmpFiles.remove(threadId);
    }
}

