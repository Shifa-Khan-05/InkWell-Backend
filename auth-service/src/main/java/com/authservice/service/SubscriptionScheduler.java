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
    @Scheduled(fixedRate = 3600000) // 1 hour
    public void checkExpiredSubscriptions() {
        log.info("Cron: Checking for expired InkWell identities...");
        LocalDateTime now = LocalDateTime.now();
        
        // Optimize: Only fetch users who are PREMIUM AND have an expired date
        List<User> expiredUsers = userRepository.findByRoleAndSubscriptionEndDateBefore(ROLE_PREMIUM, now);
        
        for (User user : expiredUsers) {
            log.warn("Subscription Expired for user: {}. Reverting to READER.", user.getEmail());
            user.setRole(ROLE_READER);
            user.setMembershipLevel("FREE");
            userRepository.save(user);
        }
    }
}
