package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.SubscriptionDTO;
import org.example.model.Subscription;
import org.example.model.SubscriptionId;
import org.example.service.SubscriptionService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    public Subscription createSubscription(@RequestBody SubscriptionDTO subscription) {
        return subscriptionService.createSubscription(subscription.getUserId(), subscription.getFolloweeId());
    }

    @DeleteMapping
    public void deleteSubscription(@RequestBody SubscriptionDTO subscription) {
        subscriptionService.deleteSubscription(SubscriptionId.builder()
                .userId(subscription.getUserId())
                .followeeId(subscription.getFolloweeId())
                .build());
    }
}
