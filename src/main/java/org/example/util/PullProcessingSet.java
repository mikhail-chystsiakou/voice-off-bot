package org.example.util;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class PullProcessingSet {
    Set<Long> pullProcessingUserIds = new HashSet<>();

    public synchronized boolean getAndSetPullProcessingForUser(long userId) {
        return pullProcessingUserIds.add(userId);
    }
    public synchronized void finishProcessingForUser(long userId) {
        pullProcessingUserIds.remove(userId);
    }
}
