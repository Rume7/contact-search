package com.codehacks.contactsearch.config;

import com.codehacks.contactsearch.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ScheduledConfig {

    private final PasswordResetService passwordResetService;

    /**
     * Clean up expired password reset tokens every hour
     */
    @Scheduled(fixedRate = 3600000) // 1 hour in milliseconds
    public void cleanupExpiredTokens() {
        try {
            passwordResetService.cleanupExpiredTokens();
            log.debug("Expired password reset tokens cleaned up");
        } catch (Exception e) {
            log.error("Error cleaning up expired tokens", e);
        }
    }
} 