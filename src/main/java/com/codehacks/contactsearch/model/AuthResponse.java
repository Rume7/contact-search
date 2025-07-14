package com.codehacks.contactsearch.model;

import java.time.LocalDateTime;

/**
 * Authentication response containing JWT token and user info
 * @param token JWT access token
 * @param refreshToken JWT refresh token
 * @param username User's username
 * @param email User's email
 * @param role User's role
 * @param expiresIn Token expiration time in seconds
 * @param timestamp When the response was generated
 */
public record AuthResponse(
    String token,
    String refreshToken,
    String username,
    String email,
    Role role,
    long expiresIn,
    LocalDateTime timestamp
) {
    public static AuthResponse of(String token, String refreshToken, String username, 
                                String email, Role role, long expiresIn) {
        return new AuthResponse(token, refreshToken, username, email, role, expiresIn, LocalDateTime.now());
    }
} 