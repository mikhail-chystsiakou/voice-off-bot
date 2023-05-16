package org.example.converter.impl;

import org.example.converter.Converter;
import org.example.dto.SubscriptionDTO;
import org.example.dto.UserDTO;
import org.example.model.Subscription;
import org.example.model.UserInfo;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionConverter implements Converter<Subscription, SubscriptionDTO> {
    @Override
    public Subscription fromDTO(SubscriptionDTO subscriptionDTO) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public SubscriptionDTO toDTO(Subscription subscription) {
        return SubscriptionDTO.builder()
                .userId(subscription.getUserInfo().getUserId())
                .followeeId(subscription.getFollowee().getUserId())
                .build();
    }
}
