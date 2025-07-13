package com.codehacks.contactsearch.model;

/**
 * Response for sync operations
 * @param message Short status message
 * @param description Detailed description of the sync operation
 */
public record SyncResponse(String message, String description) {
} 