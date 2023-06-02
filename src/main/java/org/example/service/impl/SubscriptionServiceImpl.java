package org.example.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.exception.EntityAlreadyExistsException;
import org.example.exception.EntityNotFoundException;
import org.example.model.Subscription;
import org.example.model.SubscriptionId;
import org.example.model.UserInfo;
import org.example.repository.SubscriptionRepository;
import org.example.service.SubscriptionService;
import org.example.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;

import static org.example.constant.EntityName.SUBSCRIPTION;
import static org.example.constant.ExceptionMessageConstant.ENTITY_BY_ID_IS_NOT_FOUND;
import static org.example.constant.ExceptionMessageConstant.ENTITY_WITH_SUCH_ID_ALREADY_EXISTS;

@Service
@Slf4j
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    private final UserService userService;

    public Subscription getSubscriptionById(SubscriptionId subscriptionId) throws EntityNotFoundException {
        return subscriptionRepository
                .findById(subscriptionId)
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format(ENTITY_BY_ID_IS_NOT_FOUND,
                                SUBSCRIPTION,
                                subscriptionId
                        )));
    }

    public boolean isSubscriptionWithSuchIdExists(SubscriptionId subscriptionId) {
        try {
            getSubscriptionById(subscriptionId);
            return true;
        } catch (EntityNotFoundException exception) {
            return false;
        }
    }

    @Override
    @Transactional
    public Subscription createSubscription(Long followerId, Long followeeId) throws EntityNotFoundException, EntityAlreadyExistsException {
        Subscription subscription = subscriptionRepository.save(createNewSubscription(followerId, followeeId));
        log.info("createSubscription: created subscription {}", subscription);
        return subscription;
    }

    @Override
    public Subscription deleteSubscription(SubscriptionId subscriptionId) throws EntityNotFoundException {
        Subscription subscription = getSubscriptionById(subscriptionId);
        subscriptionRepository.delete(subscription);
        log.info("deleteSubscription: deleted subscription with id {}", subscriptionId);
        return subscription;
    }

    private Subscription createNewSubscription(Long followerId, Long followeeId) throws EntityNotFoundException, EntityAlreadyExistsException {
        UserInfo follower = userService.getUserById(followerId);
        UserInfo followee = userService.getUserById(followeeId);
        SubscriptionId id = SubscriptionId.builder().userId(followerId).followeeId(followeeId).build();
        if (isSubscriptionWithSuchIdExists(id)) {
            throw new EntityAlreadyExistsException(String.format(ENTITY_WITH_SUCH_ID_ALREADY_EXISTS, SUBSCRIPTION, id));
        }
        return Subscription.builder()
                .id(id)
                .userInfo(follower)
                .followee(followee)
                .lastPull(new Timestamp(System.currentTimeMillis()))
                .build();
    }
}
