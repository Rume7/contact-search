package com.codehacks.contactsearch.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PasswordResetService {

    private final Map<String, PasswordResetToken> resetTokens = new ConcurrentHashMap<>();
    private final SecureRandom random = new SecureRandom();

    private static final int TOKEN_LENGTH = 32;
    private static final int TOKEN_EXPIRY_HOURS = 1; // 1 hour expiry

    public String generateResetToken(String email) {
        // Generate a secure random token
        String token = generateSecureToken();

        // Store token with expiry
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .email(email)
                .token(token)
                .expiry(LocalDateTime.now().plusHours(TOKEN_EXPIRY_HOURS))
                .build();

        resetTokens.put(token, resetToken);

        return token;
    }

    public boolean isValidResetToken(String token) {
        if (token == null) {
            return false;
        }

        PasswordResetToken resetToken = resetTokens.get(token);
        if (resetToken == null) {
            return false;
        }

        // Check if token is expired
        if (LocalDateTime.now().isAfter(resetToken.getExpiry())) {
            resetTokens.remove(token);
            return false;
        }

        return true;
    }

    public String getEmailForToken(String token) {
        if (token == null) {
            return null;
        }

        PasswordResetToken resetToken = resetTokens.get(token);
        return resetToken != null ? resetToken.getEmail() : null;
    }

    public void invalidateToken(String token) {
        if (token != null) {
            resetTokens.remove(token);
        }
    }

    public void cleanupExpiredTokens() {
        LocalDateTime now = LocalDateTime.now();
        resetTokens.entrySet().removeIf(entry ->
                entry.getValue().getExpiry().isBefore(now)
        );
    }

    private String generateSecureToken() {
        StringBuilder token = new StringBuilder(TOKEN_LENGTH);
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

        for (int i = 0; i < TOKEN_LENGTH; i++) {
            token.append(chars.charAt(random.nextInt(chars.length())));
        }

        return token.toString();
    }

    // Inner class to represent a password reset token
    private static class PasswordResetToken {
        private String email;
        private String tokenValue;
        private LocalDateTime expiry;

        // Builder pattern
        public static PasswordResetTokenBuilder builder() {
            return new PasswordResetTokenBuilder();
        }

        public String getEmail() {
            return email;
        }

        public String getToken() {
            return tokenValue;
        }

        public LocalDateTime getExpiry() {
            return expiry;
        }

        public static class PasswordResetTokenBuilder {
            private PasswordResetToken token = new PasswordResetToken();

            public PasswordResetTokenBuilder email(String email) {
                token.email = email;
                return this;
            }

            public PasswordResetTokenBuilder token(String tokenValue) {
                token.tokenValue = tokenValue;
                return this;
            }

            public PasswordResetTokenBuilder expiry(LocalDateTime expiry) {
                token.expiry = expiry;
                return this;
            }

            public PasswordResetToken build() {
                return token;
            }
        }
    }
} 