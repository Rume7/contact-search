package com.codehacks.contactsearch.controller;

import com.codehacks.contactsearch.document.ContactDocument;
import com.codehacks.contactsearch.service.ContactSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/api/search")
public class ContactSearchController {

    @Autowired
    private ContactSearchService contactSearchService;

    @GetMapping("/contacts")
    public ResponseEntity<List<ContactDocument>> searchContacts(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int size) {
        List<ContactDocument> results = contactSearchService.searchContacts(query, size);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/contacts/autocomplete")
    public ResponseEntity<List<ContactDocument>> autocompleteSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int size) {
        List<ContactDocument> results = contactSearchService.autocompleteSearch(query, size);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/contacts/fuzzy")
    public ResponseEntity<List<ContactDocument>> fuzzySearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "10") int size) {
        List<ContactDocument> results = contactSearchService.fuzzySearch(query, size);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/contacts/city")
    public ResponseEntity<List<ContactDocument>> searchByCity(
            @RequestParam String city,
            @RequestParam(defaultValue = "10") int size) {
        List<ContactDocument> results = contactSearchService.searchByCity(city, size);
        return ResponseEntity.ok(results);
    }
}
