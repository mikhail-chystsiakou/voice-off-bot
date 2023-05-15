package org.example.service;

import org.example.exception.EntityAlreadyExistsException;
import org.example.exception.EntityNotFoundException;
import org.example.model.Subscription;
import org.example.model.SubscriptionId;

public interface SubscriptionService {

    Subscription createSubscription(Long followerId, Long followeeId) throws EntityNotFoundException, EntityAlreadyExistsException;

    void deleteSubscription(SubscriptionId subscriptionId);
}
