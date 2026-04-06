package com.backend.scheduler;

import com.backend.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.backend.repository.UserRepository;
import com.backend.repository.ActivationTokenRepository;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UserScheduler {

    private final UserRepository userRepository;
    private final ActivationTokenRepository tokenRepository;

    // 1. Deactivate inactive users (90 days)
    @Scheduled(cron = "0 0 0 * * ?") // daily at midnight
    public void deactivateInactiveUsers() {

        List<User> users = userRepository.findAll();

        int count = 0;

        for (User user : users) {

            // skip already inactive or deleted users
            if (user.getStatus() != AccountStatus.ACTIVE) continue;

            if (user.getLastLogin() != null &&
                    user.getLastLogin().isBefore(LocalDateTime.now().minusDays(90))) {

                user.setStatus(AccountStatus.INACTIVE);
                user.setDeactivatedBy(DeactivationReason.SYSTEM);
                user.setTokenVersion(user.getTokenVersion() + 1);
                count++;
            }
        }

        userRepository.saveAll(users);

        System.out.println("Inactive users deactivated: " + count);
    }

    // 2. Clean expired activation tokens
    @Scheduled(cron = "0 0 * * * ?") // every 1 hour
    public void cleanExpiredTokens() {

        List<ActivationToken> tokens = tokenRepository.findAll();

        List<ActivationToken> expiredTokens = tokens.stream()
                .filter(t ->
                        t.getExpiry().isBefore(LocalDateTime.now())
                                || t.isUsed() // also delete used tokens
                )
                .toList();

        tokenRepository.deleteAll(expiredTokens);

        System.out.println("Expired/used activation tokens cleaned: " + expiredTokens.size());
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void processDeletedUsers() {

        List<User> users = userRepository.findAll();

        for (User user : users) {

            if (user.getDeletedAt() != null
                    && user.getStatus() == AccountStatus.INACTIVE
                    && user.getDeletedAt().isBefore(LocalDateTime.now().minusDays(30))) {

                user.setStatus(AccountStatus.DELETED);
                user.setDeleted(true);
                user.setDeactivatedBy(DeactivationReason.USER);
            }
        }

        userRepository.saveAll(users);
    }

}