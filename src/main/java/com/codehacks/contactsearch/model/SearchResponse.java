package com.codehacks.contactsearch.model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response for search operations with metadata
 * @param query The original search query
 * @param results The search results
 * @param totalResults Total number of results found
 * @param searchTimeMs Time taken for the search in milliseconds
 * @param timestamp When the search was performed
 */
public record SearchResponse<T>(
    String query,
    List<T> results,
    long totalResults,
    long searchTimeMs,
    LocalDateTime timestamp
) {
    public static <T> SearchResponse<T> of(String query, List<T> results, long totalResults, long searchTimeMs) {
        return new SearchResponse<>(query, results, totalResults, searchTimeMs, LocalDateTime.now());
    }
} 