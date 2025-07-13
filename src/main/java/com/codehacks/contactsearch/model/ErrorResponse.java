package com.codehacks.contactsearch.model;

import java.time.LocalDateTime;

/**
 * Standard error response
 * @param error The error type/code
 * @param message Human-readable error message
 * @param details Additional error details
 * @param timestamp When the error occurred
 */
public record ErrorResponse(
    String error,
    String message,
    String details,
    LocalDateTime timestamp
) {
    public static ErrorResponse of(String error, String message) {
        return new ErrorResponse(error, message, null, LocalDateTime.now());
    }
    
    public static ErrorResponse of(String error, String message, String details) {
        return new ErrorResponse(error, message, details, LocalDateTime.now());
    }
} 