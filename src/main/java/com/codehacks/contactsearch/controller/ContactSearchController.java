package com.codehacks.contactsearch.controller;

import com.codehacks.contactsearch.document.ContactDocument;
import com.codehacks.contactsearch.service.ContactSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search/contacts")
@Tag(name = "Contact Search", description = "APIs for searching contacts using Elasticsearch")
public class ContactSearchController {


    private final ContactSearchService contactSearchService;

    public ContactSearchController(ContactSearchService contactSearchService) {
        this.contactSearchService = contactSearchService;
    }

    @GetMapping
    @Operation(
        summary = "Search contacts",
        description = "Performs a full-text search across all contact fields using Elasticsearch"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Search results retrieved successfully",
            content = @Content(schema = @Schema(implementation = ContactDocument.class)))
    })
    public ResponseEntity<List<ContactDocument>> searchContacts(
            @Parameter(description = "Search query", required = true, example = "john")
            @RequestParam String query,
            @Parameter(description = "Maximum number of results", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        List<ContactDocument> results = contactSearchService.searchContacts(query, size);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/autocomplete")
    @Operation(
        summary = "Autocomplete search",
        description = "Provides autocomplete suggestions based on partial text input"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Autocomplete suggestions retrieved successfully",
            content = @Content(schema = @Schema(implementation = ContactDocument.class)))
    })
    public ResponseEntity<List<ContactDocument>> autocompleteSearch(
            @Parameter(description = "Partial search query", required = true, example = "jo")
            @RequestParam String query,
            @Parameter(description = "Maximum number of suggestions", example = "5")
            @RequestParam(defaultValue = "5") int size) {
        List<ContactDocument> results = contactSearchService.autocompleteSearch(query, size);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/fuzzy")
    @Operation(
        summary = "Fuzzy search",
        description = "Performs fuzzy search to find contacts even with typos or spelling variations"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fuzzy search results retrieved successfully",
            content = @Content(schema = @Schema(implementation = ContactDocument.class)))
    })
    public ResponseEntity<List<ContactDocument>> fuzzySearch(
            @Parameter(description = "Search query (tolerant to typos)", required = true, example = "jhon")
            @RequestParam String query,
            @Parameter(description = "Maximum number of results", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        List<ContactDocument> results = contactSearchService.fuzzySearch(query, size);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/city")
    @Operation(
        summary = "Search by city",
        description = "Searches for contacts in a specific city"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "City search results retrieved successfully",
            content = @Content(schema = @Schema(implementation = ContactDocument.class)))
    })
    public ResponseEntity<List<ContactDocument>> searchByCity(
            @Parameter(description = "City name to search for", required = true, example = "New York")
            @RequestParam String city,
            @Parameter(description = "Maximum number of results", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        List<ContactDocument> results = contactSearchService.searchByCity(city, size);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/spelling-correction")
    @Operation(
        summary = "Spelling correction search",
        description = "Advanced search that handles common misspellings of names and cities with enhanced fuzzy matching"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Spelling correction search results retrieved successfully",
            content = @Content(schema = @Schema(implementation = ContactDocument.class)))
    })
    public ResponseEntity<List<ContactDocument>> spellingCorrectionSearch(
            @Parameter(description = "Search query with potential misspellings", required = true, example = "Jhon Smith")
            @RequestParam String query,
            @Parameter(description = "Maximum number of results", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        List<ContactDocument> results = contactSearchService.spellingCorrectionSearch(query, size);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/partial-match")
    @Operation(
        summary = "Partial/Shortened name search",
        description = "Search contacts by partial or shortened names using n-gram and wildcard matching"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Partial match search results retrieved successfully",
            content = @Content(schema = @Schema(implementation = ContactDocument.class)))
    })
    public ResponseEntity<List<ContactDocument>> partialMatchSearch(
            @Parameter(description = "Partial or shortened name", required = true, example = "Alex MacSmith")
            @RequestParam String query,
            @Parameter(description = "Maximum number of results", example = "10")
            @RequestParam(defaultValue = "10") int size) {
        List<ContactDocument> results = contactSearchService.partialMatchSearch(query, size);
        return ResponseEntity.ok(results);
    }
}
