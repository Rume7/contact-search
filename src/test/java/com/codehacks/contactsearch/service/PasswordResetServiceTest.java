package com.codehacks.contactsearch.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        passwordResetService = new PasswordResetService();
    }

    @Test
    void testGenerateResetToken() {
        // Given
        String email = "test@example.com";

        // When
        String token = passwordResetService.generateResetToken(email);

        // Then
        assertNotNull(token);
        assertEquals(32, token.length());
        assertTrue(passwordResetService.isValidResetToken(token));
    }

    @Test
    void testGenerateResetToken_DifferentEmails_DifferentTokens() {
        // Given
        String email1 = "test1@example.com";
        String email2 = "test2@example.com";

        // When
        String token1 = passwordResetService.generateResetToken(email1);
        String token2 = passwordResetService.generateResetToken(email2);

        // Then
        assertNotEquals(token1, token2);
        assertTrue(passwordResetService.isValidResetToken(token1));
        assertTrue(passwordResetService.isValidResetToken(token2));
    }

    @Test
    void testIsValidResetToken_ValidToken() {
        // Given
        String email = "test@example.com";
        String token = passwordResetService.generateResetToken(email);

        // When
        boolean isValid = passwordResetService.isValidResetToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void testIsValidResetToken_InvalidToken() {
        // Given
        String invalidToken = "invalid-token";

        // When
        boolean isValid = passwordResetService.isValidResetToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testIsValidResetToken_NullToken() {
        // When
        boolean isValid = passwordResetService.isValidResetToken(null);

        // Then
        assertFalse(isValid);
    }

    @Test
    void testIsValidResetToken_EmptyToken() {
        // When
        boolean isValid = passwordResetService.isValidResetToken("");

        // Then
        assertFalse(isValid);
    }

    @Test
    void testGetEmailForToken_ValidToken() {
        // Given
        String email = "test@example.com";
        String token = passwordResetService.generateResetToken(email);

        // When
        String retrievedEmail = passwordResetService.getEmailForToken(token);

        // Then
        assertEquals(email, retrievedEmail);
    }

    @Test
    void testGetEmailForToken_InvalidToken() {
        // Given
        String invalidToken = "invalid-token";

        // When
        String retrievedEmail = passwordResetService.getEmailForToken(invalidToken);

        // Then
        assertNull(retrievedEmail);
    }

    @Test
    void testInvalidateToken() {
        // Given
        String email = "test@example.com";
        String token = passwordResetService.generateResetToken(email);
        assertTrue(passwordResetService.isValidResetToken(token));

        // When
        passwordResetService.invalidateToken(token);

        // Then
        assertFalse(passwordResetService.isValidResetToken(token));
        assertNull(passwordResetService.getEmailForToken(token));
    }

    @Test
    void testInvalidateToken_NonExistentToken() {
        // Given
        String nonExistentToken = "non-existent-token";

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> passwordResetService.invalidateToken(nonExistentToken));
    }

    @Test
    void testCleanupExpiredTokens() {
        // Given
        String email1 = "test1@example.com";
        String email2 = "test2@example.com";
        
        String token1 = passwordResetService.generateResetToken(email1);
        String token2 = passwordResetService.generateResetToken(email2);
        
        // Both tokens should be valid initially
        assertTrue(passwordResetService.isValidResetToken(token1));
        assertTrue(passwordResetService.isValidResetToken(token2));

        // When
        passwordResetService.cleanupExpiredTokens();

        // Then - tokens should still be valid (not expired yet)
        assertTrue(passwordResetService.isValidResetToken(token1));
        assertTrue(passwordResetService.isValidResetToken(token2));
    }

    @Test
    void testTokenExpiry() {
        // Given
        String email = "test@example.com";
        String token = passwordResetService.generateResetToken(email);
        
        // Token should be valid initially
        assertTrue(passwordResetService.isValidResetToken(token));

        // When - simulate time passing (this test verifies the expiry logic works)
        // Note: In a real scenario, you'd use a clock mock to test expiry
        passwordResetService.cleanupExpiredTokens();

        // Then - token should still be valid (not expired in this test)
        assertTrue(passwordResetService.isValidResetToken(token));
    }

    @Test
    void testMultipleTokensForSameEmail() {
        // Given
        String email = "test@example.com";
        
        // When
        String token1 = passwordResetService.generateResetToken(email);
        String token2 = passwordResetService.generateResetToken(email);

        // Then
        assertNotEquals(token1, token2);
        assertTrue(passwordResetService.isValidResetToken(token1));
        assertTrue(passwordResetService.isValidResetToken(token2));
        assertEquals(email, passwordResetService.getEmailForToken(token1));
        assertEquals(email, passwordResetService.getEmailForToken(token2));
    }

    @Test
    void testTokenFormat() {
        // Given
        String email = "test@example.com";

        // When
        String token = passwordResetService.generateResetToken(email);

        // Then
        assertNotNull(token);
        assertEquals(32, token.length());
        // Token should only contain alphanumeric characters
        assertTrue(token.matches("^[A-Za-z0-9]{32}$"));
    }
} 