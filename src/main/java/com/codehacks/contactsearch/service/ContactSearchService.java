package com.codehacks.contactsearch.service;

import com.codehacks.contactsearch.document.ContactDocument;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class ContactSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public ContactSearchService(ElasticsearchOperations elasticsearchOperations) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public List<ContactDocument> searchContacts(String query, int size) {
        String queryString = buildMultiMatchQueryString(query);
        Query searchQuery = new StringQuery(queryString);

        SearchHits<ContactDocument> searchHits = elasticsearchOperations.search(
                searchQuery, ContactDocument.class
        );

        return searchHits.stream()
                .map(SearchHit::getContent)
                .limit(size)
                .collect(Collectors.toList());
    }

    public List<ContactDocument> autocompleteSearch(String query, int size) {
        String queryString = buildAutocompleteQueryString(query);
        Query searchQuery = new StringQuery(queryString);

        SearchHits<ContactDocument> searchHits = elasticsearchOperations.search(
                searchQuery, ContactDocument.class
        );

        return searchHits.stream()
                .map(SearchHit::getContent)
                .limit(size)
                .collect(Collectors.toList());
    }

    public List<ContactDocument> fuzzySearch(String query, int size) {
        String queryString = buildFuzzyQueryString(query);
        Query searchQuery = new StringQuery(queryString);

        SearchHits<ContactDocument> searchHits = elasticsearchOperations.search(
                searchQuery, ContactDocument.class
        );

        return searchHits.stream()
                .map(SearchHit::getContent)
                .limit(size)
                .collect(Collectors.toList());
    }

    public List<ContactDocument> searchByCity(String city, int size) {
        String queryString = String.format("""
            {
                "term": {
                    "city.keyword": "%s"
                }
            }
            """, city);

        Query searchQuery = new StringQuery(queryString);

        SearchHits<ContactDocument> searchHits = elasticsearchOperations.search(
                searchQuery, ContactDocument.class
        );

        return searchHits.stream()
                .map(SearchHit::getContent)
                .limit(size)
                .collect(Collectors.toList());
    }

    public List<ContactDocument> spellingCorrectionSearch(String query, int size) {
        String queryString = buildSpellingCorrectionQueryString(query);
        Query searchQuery = new StringQuery(queryString);

        SearchHits<ContactDocument> searchHits = elasticsearchOperations.search(
                searchQuery, ContactDocument.class
        );

        return searchHits.stream()
                .map(SearchHit::getContent)
                .limit(size)
                .collect(Collectors.toList());
    }

    public List<ContactDocument> partialMatchSearch(String query, int size) {
        String queryString = buildPartialMatchQueryString(query);
        Query searchQuery = new StringQuery(queryString);

        SearchHits<ContactDocument> searchHits = elasticsearchOperations.search(
                searchQuery, ContactDocument.class
        );

        return searchHits.stream()
                .map(SearchHit::getContent)
                .limit(size)
                .collect(Collectors.toList());
    }

    private String buildMultiMatchQueryString(String query) {
        return String.format("""
            {
                "multi_match": {
                    "query": "%s",
                    "fields": ["firstName^3", "lastName^3", "email^2", "city"],
                    "type": "best_fields",
                    "fuzziness": "AUTO"
                }
            }
            """, query);
    }

    private String buildAutocompleteQueryString(String query) {
        return String.format("""
            {
                "multi_match": {
                    "query": "%s",
                    "fields": ["firstName.autocomplete^3", "lastName.autocomplete^3", "email.autocomplete^2", "city.autocomplete"],
                    "type": "bool_prefix"
                }
            }
            """, query);
    }

    private String buildFuzzyQueryString(String query) {
        return String.format("""
            {
                "bool": {
                    "should": [
                        {
                            "multi_match": {
                                "query": "%s",
                                "fields": ["firstName^3", "lastName^3", "email^2", "city"],
                                "fuzziness": "AUTO",
                                "minimum_should_match": "75%%"
                            }
                        },
                        {
                            "multi_match": {
                                "query": "%s",
                                "fields": ["firstName.keyword^2", "lastName.keyword^2", "email^2", "city.keyword"],
                                "type": "best_fields"
                            }
                        }
                    ]
                }
            }
            """, query, query);
    }

    private String buildSpellingCorrectionQueryString(String query) {
        return String.format("""
            {
                "bool": {
                    "should": [
                        {
                            "multi_match": {
                                "query": "%s",
                                "fields": ["firstName^3", "lastName^3", "email^2", "city"],
                                "fuzziness": "AUTO",
                                "minimum_should_match": "60%%"
                            }
                        },
                        {
                            "multi_match": {
                                "query": "%s",
                                "fields": ["firstName.keyword^2", "lastName.keyword^2", "email^2", "city.keyword"],
                                "type": "best_fields"
                            }
                        },
                        {
                            "match": {
                                "firstName": {
                                    "query": "%s",
                                    "fuzziness": "AUTO",
                                    "prefix_length": 1
                                }
                            }
                        },
                        {
                            "match": {
                                "lastName": {
                                    "query": "%s",
                                    "fuzziness": "AUTO",
                                    "prefix_length": 1
                                }
                            }
                        },
                        {
                            "match": {
                                "city": {
                                    "query": "%s",
                                    "fuzziness": "AUTO",
                                    "prefix_length": 1
                                }
                            }
                        },
                        {
                            "wildcard": {
                                "firstName": {
                                    "value": "*%s*"
                                }
                            }
                        },
                        {
                            "wildcard": {
                                "lastName": {
                                    "value": "*%s*"
                                }
                            }
                        },
                        {
                            "wildcard": {
                                "city": {
                                    "value": "*%s*"
                                }
                            }
                        }
                    ],
                    "minimum_should_match": 1
                }
            }
            """, query, query, query, query, query, query, query, query);
    }

    private String buildPartialMatchQueryString(String query) {
        return String.format("""
            {
                "bool": {
                    "should": [
                        {
                            "multi_match": {
                                "query": "%s",
                                "fields": [
                                    "firstName^3",
                                    "lastName^3",
                                    "firstName.ngram^2",
                                    "lastName.ngram^2",
                                    "email^2",
                                    "city"
                                ],
                                "type": "best_fields",
                                "minimum_should_match": "50%%"
                            }
                        },
                        {
                            "multi_match": {
                                "query": "%s",
                                "fields": [
                                    "firstName.autocomplete^3",
                                    "lastName.autocomplete^3",
                                    "email.autocomplete^2",
                                    "city.autocomplete"
                                ],
                                "type": "bool_prefix"
                            }
                        },
                        {
                            "wildcard": {
                                "firstName": {
                                    "value": "*%s*"
                                }
                            }
                        },
                        {
                            "wildcard": {
                                "lastName": {
                                    "value": "*%s*"
                                }
                            }
                        },
                        {
                            "wildcard": {
                                "email": {
                                    "value": "*%s*"
                                }
                            }
                        },
                        {
                            "wildcard": {
                                "city": {
                                    "value": "*%s*"
                                }
                            }
                        },
                        {
                            "prefix": {
                                "firstName": {
                                    "value": "%s"
                                }
                            }
                        },
                        {
                            "prefix": {
                                "lastName": {
                                    "value": "%s"
                                }
                            }
                        },
                        {
                            "prefix": {
                                "email": {
                                    "value": "%s"
                                }
                            }
                        },
                        {
                            "prefix": {
                                "city": {
                                    "value": "%s"
                                }
                            }
                        }
                    ],
                    "minimum_should_match": 1
                }
            }
            """, query, query, query, query, query, query, query, query, query, query);
    }
}