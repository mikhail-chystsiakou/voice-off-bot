package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.converter.Converter;
import org.example.dto.SubscriptionDTO;
import org.example.dto.SubscriptionWithUsersDTO;
import org.example.exception.EntityAlreadyExistsException;
import org.example.exception.EntityNotFoundException;
import org.example.model.Subscription;
import org.example.model.SubscriptionId;
import org.example.service.SubscriptionService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("api/v1/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    private final Converter<Subscription, SubscriptionWithUsersDTO> converter;

    @PostMapping
    public SubscriptionWithUsersDTO createSubscription(@RequestBody @Valid SubscriptionDTO subscription) throws EntityNotFoundException, EntityAlreadyExistsException {
        return converter.toDTO(
                subscriptionService.createSubscription(subscription.getUserId(), subscription.getFolloweeId())
        );
    }

    @DeleteMapping
    public void deleteSubscription(@RequestBody @Valid SubscriptionDTO subscription) {
        subscriptionService.deleteSubscription(SubscriptionId.builder()
                .userId(subscription.getUserId())
                .followeeId(subscription.getFolloweeId())
                .build());
    }
}
