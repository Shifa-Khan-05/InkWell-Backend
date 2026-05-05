package com.authservice.service;

import com.authservice.entity.User;
import com.authservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionSchedulerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SubscriptionScheduler subscriptionScheduler;

    @Test
    void checkExpiredSubscriptions_RevertsExpiredUser() {
        User user = new User();
        user.setEmail("expired@test.com");
        user.setRole("ROLE_PREMIUM");
        user.setSubscriptionEndDate(LocalDateTime.now().minusDays(1));

        when(userRepository.findAll()).thenReturn(List.of(user));

        subscriptionScheduler.checkExpiredSubscriptions();

        verify(userRepository).save(user);
        assert user.getRole().equals("ROLE_READER");
        assert user.getMembershipLevel().equals("FREE");
    }

    @Test
    void checkExpiredSubscriptions_SkipsActiveUser() {
        User user = new User();
        user.setRole("ROLE_PREMIUM");
        user.setSubscriptionEndDate(LocalDateTime.now().plusDays(1));

        when(userRepository.findAll()).thenReturn(List.of(user));

        subscriptionScheduler.checkExpiredSubscriptions();

        verify(userRepository, never()).save(any());
    }

    @Test
    void checkExpiredSubscriptions_SkipsNonPremiumUser() {
        User user = new User();
        user.setRole("ROLE_READER");
        user.setSubscriptionEndDate(LocalDateTime.now().minusDays(1));

        when(userRepository.findAll()).thenReturn(List.of(user));

        subscriptionScheduler.checkExpiredSubscriptions();

        verify(userRepository, never()).save(any());
    }
}
