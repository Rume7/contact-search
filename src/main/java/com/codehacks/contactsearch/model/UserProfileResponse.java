package com.codehacks.contactsearch.model;

import java.time.LocalDateTime;

/**
 * Response for user profile information
 * @param username User's username
 * @param email User's email address
 * @param firstName User's first name
 * @param lastName User's last name
 * @param role User's role
 * @param createdAt When the user was created
 * @param updatedAt When the user was last updated
 */
public record UserProfileResponse(
    String username,
    String email,
    String firstName,
    String lastName,
    Role role,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static UserProfileResponse of(String username, String email, String firstName, 
                                       String lastName, Role role, LocalDateTime createdAt, 
                                       LocalDateTime updatedAt) {
        return new UserProfileResponse(username, email, firstName, lastName, role, createdAt, updatedAt);
    }
} 