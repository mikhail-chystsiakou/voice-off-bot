package org.example.converter.impl;

import lombok.RequiredArgsConstructor;
import org.example.converter.Converter;
import org.example.dto.SubscriptionWithUsersDTO;
import org.example.dto.UserDTO;
import org.example.model.Subscription;
import org.example.model.SubscriptionId;
import org.example.model.UserInfo;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscriptionWithUserConverter implements Converter<Subscription, SubscriptionWithUsersDTO> {

    private final Converter<UserInfo, UserDTO> converter;

    @Override
    public Subscription fromDTO(SubscriptionWithUsersDTO subscriptionWithUsersDTO) {
        return Subscription.builder()
                .id(SubscriptionId.builder()
                        .userId(subscriptionWithUsersDTO.getFollower().getId())
                        .followeeId(subscriptionWithUsersDTO.getFollowee().getId())
                        .build())
                .userInfo(converter.fromDTO(subscriptionWithUsersDTO.getFollower()))
                .followee(converter.fromDTO(subscriptionWithUsersDTO.getFollowee()))
                .build();
    }

    @Override
    public SubscriptionWithUsersDTO toDTO(Subscription subscription) {
        return SubscriptionWithUsersDTO.builder()
                .follower(converter.toDTO(subscription.getUserInfo()))
                .followee(converter.toDTO(subscription.getFollowee()))
                .build();
    }
}
