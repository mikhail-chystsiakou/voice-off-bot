package org.example.service;

import org.example.model.Subscription;
import org.example.model.SubscriptionId;

public interface SubscriptionService {
    Subscription createSubscription(Long followerId, Long followeeId);

    void deleteSubscription(SubscriptionId subscriptionId);
}
