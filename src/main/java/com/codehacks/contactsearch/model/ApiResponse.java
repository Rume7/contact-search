package com.codehacks.contactsearch.model;

import java.time.LocalDateTime;

/**
 * Generic API response wrapper
 * @param success Whether the operation was successful
 * @param message Response message
 * @param data The actual response data
 * @param timestamp When the response was generated
 */
public record ApiResponse<T>(
    boolean success,
    String message,
    T data,
    LocalDateTime timestamp
) {
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data, LocalDateTime.now());
    }
    
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now());
    }
} 