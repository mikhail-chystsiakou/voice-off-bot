package org.example.service.impl;

import org.example.exception.EntityAlreadyExistsException;
import org.example.exception.EntityNotFoundException;
import org.example.model.Subscription;
import org.example.model.SubscriptionId;
import org.example.model.UserInfo;
import org.example.repository.SubscriptionRepository;
import org.example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Timestamp;
import java.util.Optional;

import static org.example.constant.ExceptionMessageConstant.ENTITY_BY_ID_IS_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    @Mock
    private UserService userService;

    private UserInfo user1;

    private UserInfo user2;

    @BeforeEach
    public void init() {
        user1 = new UserInfo();
        user1.setUserId(1L);
        user1.setChatId("1");
        user1.setUsername("user1");
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setTimezone(0);
        user1.setFeedbackModeAllowed(true);
        user1.setFeedbackModeEnabled(true);

        user2 = new UserInfo();
        user2.setUserId(2L);
        user2.setChatId("2");
        user2.setUsername("user2");
        user2.setFirstName("Johny");
        user2.setLastName("Doe");
        user2.setTimezone(0);
        user2.setFeedbackModeAllowed(true);
        user2.setFeedbackModeEnabled(true);
    }

    @Test
    public void subscriptionCanBeSaved() throws EntityAlreadyExistsException, EntityNotFoundException {
        when(userService.getUserById(eq(user1.getUserId()))).thenReturn(user1);
        when(userService.getUserById(eq(user2.getUserId()))).thenReturn(user2);

        Subscription expectedSubscription = Subscription.builder()
                .id(SubscriptionId.builder()
                        .userId(user1.getUserId())
                        .followeeId(user2.getUserId())
                        .build())
                .userInfo(user1)
                .followee(user2)
                .lastPull(new Timestamp(System.currentTimeMillis()))
                .build();

        when(subscriptionRepository.findById(expectedSubscription.getId())).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any())).thenReturn(expectedSubscription);

        Subscription subscriptionFromDb = subscriptionService.createSubscription(user1.getUserId(), user2.getUserId());
        assertEquals(expectedSubscription, subscriptionFromDb);
    }

    @Test
    public void exceptionIsThrownWhenUserNotFound() throws EntityNotFoundException {
        EntityNotFoundException expectedException =
                new EntityNotFoundException(String.format(ENTITY_BY_ID_IS_NOT_FOUND, "User", user1.getUserId()));
        when(userService.getUserById(eq(user1.getUserId()))).thenThrow(expectedException);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> subscriptionService.createSubscription(user1.getUserId(), user2.getUserId())
        );

        assertEquals(expectedException.getMessage(), exception.getMessage());
    }

}