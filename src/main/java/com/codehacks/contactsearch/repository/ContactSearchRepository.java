package com.codehacks.contactsearch.repository;

import com.codehacks.contactsearch.document.ContactDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContactSearchRepository extends ElasticsearchRepository<ContactDocument, String> {
}
