package com.codehacks.contactsearch.model;

import jakarta.validation.constraints.NotBlank;

/**
 * Authentication request for login
 * @param username Username or email
 * @param password User password
 */
public record AuthRequest(
    @NotBlank(message = "Username is required")
    String username,
    
    @NotBlank(message = "Password is required")
    String password
) {} 