package com.codehacks.contactsearch.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceTest {

    private TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUp() {
        tokenBlacklistService = new TokenBlacklistService();
    }

    @Test
    void testBlacklistToken_ShouldAddTokenToBlacklist() {
        // Given
        String token = "test.jwt.token";
        long expirationTime = System.currentTimeMillis() + 3600000; // 1 hour from now

        // When
        tokenBlacklistService.blacklistToken(token, expirationTime);

        // Then
        assertThat(tokenBlacklistService.isBlacklisted(token)).isTrue();
    }

    @Test
    void testBlacklistToken_WithMultipleTokens_ShouldHandleAllTokens() {
        // Given
        String token1 = "token1.jwt";
        String token2 = "token2.jwt";
        String token3 = "token3.jwt";
        long expirationTime = System.currentTimeMillis() + 3600000;

        // When
        tokenBlacklistService.blacklistToken(token1, expirationTime);
        tokenBlacklistService.blacklistToken(token2, expirationTime);
        tokenBlacklistService.blacklistToken(token3, expirationTime);

        // Then
        assertThat(tokenBlacklistService.isBlacklisted(token1)).isTrue();
        assertThat(tokenBlacklistService.isBlacklisted(token2)).isTrue();
        assertThat(tokenBlacklistService.isBlacklisted(token3)).isTrue();
    }

    @Test
    void testIsBlacklisted_WithNonBlacklistedToken_ShouldReturnFalse() {
        // Given
        String token = "non.blacklisted.token";

        // When
        boolean isBlacklisted = tokenBlacklistService.isBlacklisted(token);

        // Then
        assertThat(isBlacklisted).isFalse();
    }

    @Test
    void testIsBlacklisted_WithBlacklistedToken_ShouldReturnTrue() {
        // Given
        String token = "blacklisted.jwt.token";
        long expirationTime = System.currentTimeMillis() + 3600000;
        tokenBlacklistService.blacklistToken(token, expirationTime);

        // When
        boolean isBlacklisted = tokenBlacklistService.isBlacklisted(token);

        // Then
        assertThat(isBlacklisted).isTrue();
    }

    @Test
    void testIsBlacklisted_WithExpiredToken_ShouldReturnFalseAndRemoveToken() {
        // Given
        String token = "expired.jwt.token";
        long expiredTime = System.currentTimeMillis() - 1000; // 1 second ago
        tokenBlacklistService.blacklistToken(token, expiredTime);

        // When
        boolean isBlacklisted = tokenBlacklistService.isBlacklisted(token);

        // Then
        assertThat(isBlacklisted).isFalse();
        // Check that the token was removed from blacklist
        assertThat(tokenBlacklistService.isBlacklisted(token)).isFalse();
    }

    @Test
    void testIsBlacklisted_WithNullToken_ShouldReturnFalse() {
        // When
        boolean isBlacklisted = tokenBlacklistService.isBlacklisted(null);

        // Then
        assertThat(isBlacklisted).isFalse();
    }

    @Test
    void testIsBlacklisted_WithEmptyToken_ShouldReturnFalse() {
        // When
        boolean isBlacklisted = tokenBlacklistService.isBlacklisted("");

        // Then
        assertThat(isBlacklisted).isFalse();
    }

    @Test
    void testBlacklistToken_WithNullToken_ShouldHandleGracefully() {
        // Given
        long expirationTime = System.currentTimeMillis() + 3600000;

        // When & Then - Should not throw exception
        tokenBlacklistService.blacklistToken(null, expirationTime);
        
        // Verify null token is not blacklisted
        assertThat(tokenBlacklistService.isBlacklisted(null)).isFalse();
    }

    @Test
    void testBlacklistToken_WithEmptyToken_ShouldHandleGracefully() {
        // Given
        long expirationTime = System.currentTimeMillis() + 3600000;

        // When & Then - Should not throw exception
        tokenBlacklistService.blacklistToken("", expirationTime);
        
        // Verify empty token is not blacklisted
        assertThat(tokenBlacklistService.isBlacklisted("")).isFalse();
    }

    @Test
    void testCleanupExpiredTokens_ShouldRemoveExpiredTokens() {
        // Given
        String validToken = "valid.jwt.token";
        String expiredToken1 = "expired1.jwt.token";
        String expiredToken2 = "expired2.jwt.token";
        
        long validExpiration = System.currentTimeMillis() + 3600000; // 1 hour from now
        long expiredTime = System.currentTimeMillis() - 1000; // 1 second ago
        
        tokenBlacklistService.blacklistToken(validToken, validExpiration);
        tokenBlacklistService.blacklistToken(expiredToken1, expiredTime);
        tokenBlacklistService.blacklistToken(expiredToken2, expiredTime);

        // Verify all tokens are initially blacklisted
        assertThat(tokenBlacklistService.isBlacklisted(validToken)).isTrue();
        assertThat(tokenBlacklistService.isBlacklisted(expiredToken1)).isFalse(); // Already cleaned up
        assertThat(tokenBlacklistService.isBlacklisted(expiredToken2)).isFalse(); // Already cleaned up

        // When
        tokenBlacklistService.cleanupExpiredTokens();

        // Then
        assertThat(tokenBlacklistService.isBlacklisted(validToken)).isTrue();
        assertThat(tokenBlacklistService.isBlacklisted(expiredToken1)).isFalse();
        assertThat(tokenBlacklistService.isBlacklisted(expiredToken2)).isFalse();
    }

    @Test
    void testCleanupExpiredTokens_WithNoExpiredTokens_ShouldNotRemoveValidTokens() {
        // Given
        String token1 = "valid1.jwt.token";
        String token2 = "valid2.jwt.token";
        long expirationTime = System.currentTimeMillis() + 3600000;
        
        tokenBlacklistService.blacklistToken(token1, expirationTime);
        tokenBlacklistService.blacklistToken(token2, expirationTime);

        // When
        tokenBlacklistService.cleanupExpiredTokens();

        // Then
        assertThat(tokenBlacklistService.isBlacklisted(token1)).isTrue();
        assertThat(tokenBlacklistService.isBlacklisted(token2)).isTrue();
    }

    @Test
    void testCleanupExpiredTokens_WithEmptyBlacklist_ShouldNotThrowException() {
        // When & Then - Should not throw exception
        tokenBlacklistService.cleanupExpiredTokens();
    }

    @Test
    void testConcurrentAccess_ShouldHandleMultipleThreads() throws InterruptedException {
        // Given
        String token = "concurrent.jwt.token";
        long expirationTime = System.currentTimeMillis() + 3600000;
        
        // When - Multiple threads accessing the service
        Thread thread1 = new Thread(() -> {
            tokenBlacklistService.blacklistToken(token, expirationTime);
        });
        
        Thread thread2 = new Thread(() -> {
            tokenBlacklistService.isBlacklisted(token);
        });
        
        Thread thread3 = new Thread(() -> {
            tokenBlacklistService.cleanupExpiredTokens();
        });

        // Start all threads
        thread1.start();
        thread2.start();
        thread3.start();

        // Wait for all threads to complete
        thread1.join();
        thread2.join();
        thread3.join();

        // Then - Should handle concurrent access without issues
        assertThat(tokenBlacklistService.isBlacklisted(token)).isTrue();
    }

    @Test
    void testTokenExpiration_ShouldAutomaticallyRemoveExpiredTokens() throws InterruptedException {
        // Given
        String token = "short.lived.jwt.token";
        long shortExpiration = System.currentTimeMillis() + 100; // 100ms from now
        tokenBlacklistService.blacklistToken(token, shortExpiration);

        // Verify token is initially blacklisted
        assertThat(tokenBlacklistService.isBlacklisted(token)).isTrue();

        // When - Wait for token to expire
        Thread.sleep(150); // Wait longer than expiration time

        // Then - Token should be automatically removed
        assertThat(tokenBlacklistService.isBlacklisted(token)).isFalse();
    }

    @Test
    void testBlacklistToken_WithSameTokenMultipleTimes_ShouldUpdateExpiration() {
        // Given
        String token = "duplicate.jwt.token";
        long firstExpiration = System.currentTimeMillis() + 1000; // 1 second
        long secondExpiration = System.currentTimeMillis() + 3600000; // 1 hour

        // When
        tokenBlacklistService.blacklistToken(token, firstExpiration);
        tokenBlacklistService.blacklistToken(token, secondExpiration);

        // Then
        assertThat(tokenBlacklistService.isBlacklisted(token)).isTrue();
    }
} 