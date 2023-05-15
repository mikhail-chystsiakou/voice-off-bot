package org.example.service.impl;

import org.example.config.DataSourceConfig;
import org.example.exception.EntityAlreadyExistsException;
import org.example.exception.EntityNotFoundException;
import org.example.model.Subscription;
import org.example.model.SubscriptionId;
import org.example.model.UserInfo;
import org.example.repository.SubscriptionRepository;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.example.constant.ExceptionMessageConstant.ENTITY_BY_ID_IS_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock
    private DataSourceConfig dataSourceConfig;

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    @Mock
    private UserServiceImpl userService;

    private UserInfo user1;

    private UserInfo user2;

    @BeforeEach
    public void init() {
        user1 = UserInfo.builder()
                .userId(1L)
                .chatId("1")
                .username("user1")
                .firstName("John")
                .lastName("Doe")
                .timezone(0)
                .feedbackModeAllowed(true)
                .feedbackModeEnabled(true)
                .build();
        user2 = UserInfo.builder()
                .userId(2L)
                .chatId("2")
                .username("user2")
                .firstName("John")
                .lastName("Doe")
                .timezone(0)
                .feedbackModeAllowed(true)
                .feedbackModeEnabled(true)
                .build();
    }

    @Test
    public void subscriptionCanBeSaved() throws EntityAlreadyExistsException, EntityNotFoundException {
        when(userService.getUserById(user1.getUserId())).thenReturn(user1);
        when(userService.getUserById(user2.getUserId())).thenReturn(user2);

        Subscription expectedSubscription = Subscription.builder()
                .id(SubscriptionId.builder()
                        .userId(user1.getUserId())
                        .followeeId(user2.getUserId())
                        .build())
                .userInfo(user1)
                .followee(user2)
                .build();


        when(subscriptionRepository.save(expectedSubscription)).thenReturn(expectedSubscription);

        Subscription subscriptionFromDb = subscriptionService.createSubscription(user1.getUserId(), user2.getUserId());
        verify(subscriptionRepository, times(1)).save(subscriptionFromDb);
    }

    @Test
    public void exceptionIsThrownWhenUserNotFound() throws EntityNotFoundException {
        EntityNotFoundException expectedException =
                new EntityNotFoundException(String.format(ENTITY_BY_ID_IS_NOT_FOUND, "User", user1.getUserId()));
        when(userService.getUserById(1L)).thenThrow(expectedException);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> subscriptionService.createSubscription(user1.getUserId(), user2.getUserId())
        );

        assertEquals(expectedException.getMessage(), exception.getMessage());
    }

}