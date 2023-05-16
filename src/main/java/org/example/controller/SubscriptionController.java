package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.converter.Converter;
import org.example.dto.SubscriptionDTO;
import org.example.model.Subscription;
import org.example.model.SubscriptionId;
import org.example.service.SubscriptionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final Converter<Subscription, SubscriptionDTO> subscriptionConverter;

    @PostMapping
    public SubscriptionDTO createSubscription(@RequestBody SubscriptionDTO subscription) {
        return subscriptionConverter.toDTO(
                subscriptionService.createSubscription(subscription.getUserId(), subscription.getFolloweeId())
        );
    }

    @DeleteMapping
    public void deleteSubscription(@RequestBody SubscriptionDTO subscription) {
        subscriptionService.deleteSubscription(SubscriptionId.builder()
                .userId(subscription.getUserId())
                .followeeId(subscription.getFolloweeId())
                .build());
    }
}
