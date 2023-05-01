package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.model.Subscription;
import org.example.model.SubscriptionId;
import org.example.model.UserInfo;
import org.example.repository.SubscriptionRepository;
import org.example.service.SubscriptionService;
import org.example.service.UserService;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    private final UserService userService;

    @Override
    public Subscription createSubscription(Long followerId, Long followeeId) {
        UserInfo follower = userService.getUserById(followerId);
        UserInfo followee = userService.getUserById(followeeId);
        Subscription subscription = subscriptionRepository.save(Subscription.builder()
                .id(new SubscriptionId())
                .userInfo(follower)
                .followee(followee)
                .build()
        );
        log.info("createSubscription: created subscription {}", subscription);
        return subscription;
    }

    @Override
    public void deleteSubscription(SubscriptionId subscriptionId) {
        subscriptionRepository.deleteById(subscriptionId);
        log.info("deleteSubscription: deleted subscription with id {}", subscriptionId);
    }
}
