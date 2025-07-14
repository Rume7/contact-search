package com.codehacks.contactsearch.controller;

import com.codehacks.contactsearch.model.Contact;
import com.codehacks.contactsearch.service.ContactService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.codehacks.contactsearch.model.SyncResponse;

@RestController
@RequestMapping("/api/v1/contacts")
@Tag(name = "Contact Management", description = "APIs for managing contacts with PostgreSQL and Elasticsearch integration")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @PostMapping
    @Operation(
        summary = "Create a new contact",
        description = "Creates a new contact and stores it in both PostgreSQL and Elasticsearch"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contact created successfully",
            content = @Content(schema = @Schema(implementation = Contact.class))),
        @ApiResponse(responseCode = "400", description = "Invalid contact data")
    })
    public ResponseEntity<Contact> createContact(
        @Parameter(description = "Contact information to create", required = true)
        @Valid @RequestBody Contact contact) {
        Contact created = contactService.createContact(contact);
        return ResponseEntity.ok(created);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Get contact by ID",
        description = "Retrieves a contact by its unique identifier"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contact found",
            content = @Content(schema = @Schema(implementation = Contact.class))),
        @ApiResponse(responseCode = "404", description = "Contact not found")
    })
    public ResponseEntity<Contact> getContact(
        @Parameter(description = "Contact ID", required = true, example = "1")
        @PathVariable Long id) {
        return contactService.getContactById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update contact by ID",
        description = "Updates an existing contact with new information"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contact updated successfully",
            content = @Content(schema = @Schema(implementation = Contact.class))),
        @ApiResponse(responseCode = "404", description = "Contact not found"),
        @ApiResponse(responseCode = "400", description = "Invalid contact data")
    })
    public ResponseEntity<Contact> updateContact(
        @Parameter(description = "Contact ID", required = true, example = "1")
        @PathVariable Long id,
        @Parameter(description = "Updated contact information", required = true)
        @Valid @RequestBody Contact contact) {
        try {
            Contact updated = contactService.updateContact(id, contact);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete contact by ID",
        description = "Removes a contact from both PostgreSQL and Elasticsearch"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Contact deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Contact not found")
    })
    public ResponseEntity<Void> deleteContact(
        @Parameter(description = "Contact ID", required = true, example = "1")
        @PathVariable Long id) {
        contactService.deleteContact(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(
        summary = "Get all contacts",
        description = "Retrieves a paginated list of all contacts with optional sorting"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Contacts retrieved successfully",
            content = @Content(schema = @Schema(implementation = Page.class)))
    })
    public ResponseEntity<Page<Contact>> getAllContacts(
        @Parameter(description = "Pagination and sorting parameters")
        Pageable pageable) {
        return ResponseEntity.ok(contactService.getAllContacts(pageable));
    }

    @PostMapping("/sync")
    @Operation(
        summary = "Sync contacts to Elasticsearch",
        description = "Synchronizes all contacts from PostgreSQL to Elasticsearch for search functionality"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Sync completed successfully",
            content = @Content(schema = @Schema(implementation = SyncResponse.class)))
    })
    public ResponseEntity<SyncResponse> syncToElasticsearch() {
        contactService.syncAllToElasticsearch();
        return ResponseEntity.ok(new SyncResponse("Sync completed", "All contacts have been synchronized to Elasticsearch"));
    }
}
