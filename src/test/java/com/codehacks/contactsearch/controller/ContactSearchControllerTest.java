package com.codehacks.contactsearch.controller;

import com.codehacks.contactsearch.document.ContactDocument;
import com.codehacks.contactsearch.service.ContactSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ContactSearchControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ContactSearchService contactSearchService;

    @InjectMocks
    private ContactSearchController contactSearchController;

    private ContactDocument testContact1;
    private ContactDocument testContact2;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(contactSearchController).build();
        
        testContact1 = new ContactDocument(
            "1", "John", "Smith", "john.smith@email.com", 
            "New York", LocalDateTime.now(), LocalDateTime.now()
        );
        
        testContact2 = new ContactDocument(
            "2", "Jane", "Doe", "jane.doe@email.com", 
            "Los Angeles", LocalDateTime.now(), LocalDateTime.now()
        );
    }

    @Test
    void testSearchContacts() throws Exception {
        // Given
        List<ContactDocument> results = Arrays.asList(testContact1, testContact2);
        when(contactSearchService.searchContacts("John", 10)).thenReturn(results);

        // When & Then
        mockMvc.perform(get("/api/v1/search/contacts")
                .param("query", "John")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[1].firstName").value("Jane"));
    }

    @Test
    void testAutocompleteSearch() throws Exception {
        // Given
        List<ContactDocument> results = Arrays.asList(testContact1);
        when(contactSearchService.autocompleteSearch("Jo", 5)).thenReturn(results);

        // When & Then
        mockMvc.perform(get("/api/v1/search/contacts/autocomplete")
                .param("query", "Jo")
                .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    void testFuzzySearch() throws Exception {
        // Given
        List<ContactDocument> results = Arrays.asList(testContact1);
        when(contactSearchService.fuzzySearch("Jhon", 10)).thenReturn(results);

        // When & Then
        mockMvc.perform(get("/api/v1/search/contacts/fuzzy")
                .param("query", "Jhon")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    void testSearchByCity() throws Exception {
        // Given
        List<ContactDocument> results = Arrays.asList(testContact1);
        when(contactSearchService.searchByCity("New York", 10)).thenReturn(results);

        // When & Then
        mockMvc.perform(get("/api/v1/search/contacts/city")
                .param("city", "New York")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].city").value("New York"));
    }

    @Test
    void testSpellingCorrectionSearch() throws Exception {
        // Given
        List<ContactDocument> results = Arrays.asList(testContact1);
        when(contactSearchService.spellingCorrectionSearch("Jhon Smith", 10)).thenReturn(results);

        // When & Then
        mockMvc.perform(get("/api/v1/search/contacts/spelling-correction")
                .param("query", "Jhon Smith")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    void testPartialMatchSearch() throws Exception {
        // Given
        List<ContactDocument> results = Arrays.asList(testContact1);
        when(contactSearchService.partialMatchSearch("Rob", 10)).thenReturn(results);

        // When & Then
        mockMvc.perform(get("/api/v1/search/contacts/partial-match")
                .param("query", "Rob")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    void testSearchWithDefaultSize() throws Exception {
        // Given
        List<ContactDocument> results = Arrays.asList(testContact1);
        when(contactSearchService.searchContacts("John", 10)).thenReturn(results);

        // When & Then
        mockMvc.perform(get("/api/v1/search/contacts")
                .param("query", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].firstName").value("John"));
    }

    @Test
    void testSearchWithEmptyResults() throws Exception {
        // Given
        when(contactSearchService.searchContacts("NonExistent", 10)).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/v1/search/contacts")
                .param("query", "NonExistent")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
} 