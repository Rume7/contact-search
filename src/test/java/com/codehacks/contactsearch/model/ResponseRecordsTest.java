package com.codehacks.contactsearch.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ResponseRecordsTest {

    @Test
    void testSyncResponse() {
        // Given
        String message = "Sync completed";
        String description = "All contacts synchronized";

        // When
        SyncResponse response = new SyncResponse(message, description);

        // Then
        assertThat(response.message()).isEqualTo(message);
        assertThat(response.description()).isEqualTo(description);
    }

    @Test
    void testApiResponseSuccess() {
        // Given
        String message = "Operation successful";
        String data = "test data";

        // When
        ApiResponse<String> response = ApiResponse.success(message, data);

        // Then
        assertThat(response.success()).isTrue();
        assertThat(response.message()).isEqualTo(message);
        assertThat(response.data()).isEqualTo(data);
        assertThat(response.timestamp()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void testApiResponseError() {
        // Given
        String message = "Operation failed";

        // When
        ApiResponse<String> response = ApiResponse.error(message);

        // Then
        assertThat(response.success()).isFalse();
        assertThat(response.message()).isEqualTo(message);
        assertThat(response.data()).isNull();
        assertThat(response.timestamp()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void testSearchResponse() {
        // Given
        String query = "John";
        List<String> results = Arrays.asList("John Doe", "John Smith");
        long totalResults = 2;
        long searchTimeMs = 150;

        // When
        SearchResponse<String> response = SearchResponse.of(query, results, totalResults, searchTimeMs);

        // Then
        assertThat(response.query()).isEqualTo(query);
        assertThat(response.results()).isEqualTo(results);
        assertThat(response.totalResults()).isEqualTo(totalResults);
        assertThat(response.searchTimeMs()).isEqualTo(searchTimeMs);
        assertThat(response.timestamp()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void testErrorResponseWithDetails() {
        // Given
        String error = "VALIDATION_ERROR";
        String message = "Invalid input";
        String details = "Email format is invalid";

        // When
        ErrorResponse response = ErrorResponse.of(error, message, details);

        // Then
        assertThat(response.error()).isEqualTo(error);
        assertThat(response.message()).isEqualTo(message);
        assertThat(response.details()).isEqualTo(details);
        assertThat(response.timestamp()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void testErrorResponseWithoutDetails() {
        // Given
        String error = "NOT_FOUND";
        String message = "Resource not found";

        // When
        ErrorResponse response = ErrorResponse.of(error, message);

        // Then
        assertThat(response.error()).isEqualTo(error);
        assertThat(response.message()).isEqualTo(message);
        assertThat(response.details()).isNull();
        assertThat(response.timestamp()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void testRecordImmutability() {
        // Given
        SyncResponse original = new SyncResponse("Original", "Original description");

        // When & Then - Records are immutable, so we can't modify them after creation
        // This test verifies that the record behaves as expected
        assertThat(original.message()).isEqualTo("Original");
        assertThat(original.description()).isEqualTo("Original description");
    }

    @Test
    void testRecordEquality() {
        // Given
        SyncResponse response1 = new SyncResponse("Test", "Description");
        SyncResponse response2 = new SyncResponse("Test", "Description");
        SyncResponse response3 = new SyncResponse("Different", "Description");

        // When & Then
        assertThat(response1).isEqualTo(response2);
        assertThat(response1).isNotEqualTo(response3);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }
} 