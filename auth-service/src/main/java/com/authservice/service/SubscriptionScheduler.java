package com.authservice.service;

import com.authservice.entity.User;
import com.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionScheduler {

    private final UserRepository userRepository;

    private static final String ROLE_PREMIUM = "ROLE_PREMIUM";
    private static final String ROLE_READER = "ROLE_READER";

    /**
     * Runs every hour to check for expired subscriptions.
     * Reverts PREMIUM users to READER if their subscriptionEndDate has passed.
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void checkExpiredSubscriptions() {
        log.info("Cron: Checking for expired InkWell identities...");
        List<User> users = userRepository.findAll();
        
        for (User user : users) {
            if (ROLE_PREMIUM.equals(user.getRole()) && user.getSubscriptionEndDate() != null && user.getSubscriptionEndDate().isBefore(LocalDateTime.now())) {
                log.warn("Subscription Expired for user: {}. Reverting to READER.", user.getEmail());
                user.setRole(ROLE_READER);
                user.setMembershipLevel("FREE");
                userRepository.save(user);
            }
        }
    }
}
