package com.codehacks.contactsearch.service;

import com.codehacks.contactsearch.document.ContactDocument;
import com.codehacks.contactsearch.repository.ContactSearchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContactSearchServiceTest {

    @Mock
    private ContactSearchRepository contactSearchRepository;

    @Mock
    private ElasticsearchOperations elasticsearchOperations;

    @InjectMocks
    private ContactSearchService contactSearchService;

    private ContactDocument testContact1;
    private ContactDocument testContact2;

    @BeforeEach
    void setUp() {
        testContact1 = new ContactDocument(
            "1", "John", "Smith", "john.smith@email.com", 
            "New York", LocalDateTime.now(), LocalDateTime.now()
        );
        
        testContact2 = new ContactDocument(
            "2", "Jane", "Doe", "jane.doe@email.com", 
            "Los Angeles", LocalDateTime.now(), LocalDateTime.now()
        );
    }

    private SearchHit<ContactDocument> mockHit(ContactDocument doc) {
        SearchHit<ContactDocument> hit = Mockito.mock(SearchHit.class);
        when(hit.getContent()).thenReturn(doc);
        return hit;
    }

    private SearchHits<ContactDocument> mockHits(List<ContactDocument> docs) {
        List<SearchHit<ContactDocument>> hits = docs.stream().map(this::mockHit).toList();
        SearchHits<ContactDocument> searchHits = Mockito.mock(SearchHits.class);
        when(searchHits.stream()).thenReturn(hits.stream());
        return searchHits;
    }

    @Test
    void testSearchContacts() {
        // Given
        SearchHits<ContactDocument> searchHits = mockHits(Arrays.asList(testContact1, testContact2));
        when(elasticsearchOperations.search(any(Query.class), any(Class.class)))
            .thenReturn(searchHits);

        // When
        List<ContactDocument> results = contactSearchService.searchContacts("John", 10);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting("firstName").contains("John", "Jane");
    }

    @Test
    void testAutocompleteSearch() {
        // Given
        SearchHits<ContactDocument> searchHits = mockHits(Collections.singletonList(testContact1));
        when(elasticsearchOperations.search(any(Query.class), any(Class.class)))
            .thenReturn(searchHits);

        // When
        List<ContactDocument> results = contactSearchService.autocompleteSearch("Jo", 5);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    void testFuzzySearch() {
        // Given
        SearchHits<ContactDocument> searchHits = mockHits(Collections.singletonList(testContact1));
        when(elasticsearchOperations.search(any(Query.class), any(Class.class)))
            .thenReturn(searchHits);

        // When
        List<ContactDocument> results = contactSearchService.fuzzySearch("Jhon", 10);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    void testSearchByCity() {
        // Given
        SearchHits<ContactDocument> searchHits = mockHits(Collections.singletonList(testContact1));
        when(elasticsearchOperations.search(any(Query.class), any(Class.class)))
            .thenReturn(searchHits);

        // When
        List<ContactDocument> results = contactSearchService.searchByCity("New York", 10);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCity()).isEqualTo("New York");
    }

    @Test
    void testSpellingCorrectionSearch() {
        // Given
        SearchHits<ContactDocument> searchHits = mockHits(Collections.singletonList(testContact1));
        when(elasticsearchOperations.search(any(Query.class), any(Class.class)))
            .thenReturn(searchHits);

        // When
        List<ContactDocument> results = contactSearchService.spellingCorrectionSearch("Jhon Smith", 10);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    void testPartialMatchSearch() {
        // Given
        SearchHits<ContactDocument> searchHits = mockHits(Collections.singletonList(testContact1));
        when(elasticsearchOperations.search(any(Query.class), any(Class.class)))
            .thenReturn(searchHits);

        // When
        List<ContactDocument> results = contactSearchService.partialMatchSearch("Rob", 10);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getFirstName()).isEqualTo("John");
    }

    @Test
    void testSearchWithEmptyResults() {
        // Given
        SearchHits<ContactDocument> searchHits = mockHits(Collections.emptyList());
        when(elasticsearchOperations.search(any(Query.class), any(Class.class)))
            .thenReturn(searchHits);

        // When
        List<ContactDocument> results = contactSearchService.searchContacts("NonExistent", 10);

        // Then
        assertThat(results).isEmpty();
    }
} 