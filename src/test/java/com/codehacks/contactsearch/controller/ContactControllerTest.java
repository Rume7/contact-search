package com.codehacks.contactsearch.controller;

import com.codehacks.contactsearch.model.Contact;
import com.codehacks.contactsearch.service.ContactService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ContactControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ContactService contactService;

    @InjectMocks
    private ContactController contactController;

    private ObjectMapper objectMapper;
    private Contact testContact;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        
        mockMvc = MockMvcBuilders.standaloneSetup(contactController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
        
        testContact = new Contact();
        testContact.setId(1L);
        testContact.setFirstName("John");
        testContact.setLastName("Doe");
        testContact.setEmail("john.doe@test.com");
        testContact.setCity("Test City");
        testContact.setCreatedAt(LocalDateTime.now());
        testContact.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateContact() throws Exception {
        // Given
        when(contactService.createContact(any(Contact.class))).thenReturn(testContact);

        // When & Then
        mockMvc.perform(post("/api/v1/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testContact)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@test.com"))
                .andExpect(jsonPath("$.city").value("Test City"));

        verify(contactService, times(1)).createContact(any(Contact.class));
    }

    @Test
    void testGetContactById() throws Exception {
        // Given
        when(contactService.getContactById(1L)).thenReturn(Optional.of(testContact));

        // When & Then
        mockMvc.perform(get("/api/v1/contacts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(contactService, times(1)).getContactById(1L);
    }

    @Test
    void testGetContactByIdNotFound() throws Exception {
        // Given
        when(contactService.getContactById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/contacts/999"))
                .andExpect(status().isNotFound());

        verify(contactService, times(1)).getContactById(999L);
    }

    @Test
    void testUpdateContact() throws Exception {
        // Given
        Contact updatedContact = new Contact();
        updatedContact.setFirstName("Jane");
        updatedContact.setLastName("Doe");
        updatedContact.setEmail("jane.doe@test.com");
        updatedContact.setCity("New City");

        when(contactService.updateContact(1L, updatedContact)).thenReturn(updatedContact);

        // When & Then
        mockMvc.perform(put("/api/v1/contacts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedContact)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Jane"))
                .andExpect(jsonPath("$.city").value("New City"));

        verify(contactService, times(1)).updateContact(1L, updatedContact);
    }

    @Test
    void testUpdateContactNotFound() throws Exception {
        // Given
        when(contactService.updateContact(eq(999L), any(Contact.class)))
                .thenThrow(new RuntimeException("Contact not found"));

        // When & Then
        mockMvc.perform(put("/api/v1/contacts/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testContact)))
                .andExpect(status().isNotFound());

        verify(contactService, times(1)).updateContact(eq(999L), any(Contact.class));
    }

    @Test
    void testDeleteContact() throws Exception {
        // Given
        doNothing().when(contactService).deleteContact(1L);

        // When & Then
        mockMvc.perform(delete("/api/v1/contacts/1"))
                .andExpect(status().isNoContent());

        verify(contactService, times(1)).deleteContact(1L);
    }

    @Test
    void testGetAllContacts() throws Exception {
        // Given
        Page<Contact> contactPage = new PageImpl<>(
                Arrays.asList(testContact),
                PageRequest.of(0, 10),
                1
        );
        when(contactService.getAllContacts(any(PageRequest.class))).thenReturn(contactPage);

        // When & Then
        mockMvc.perform(get("/api/v1/contacts")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].firstName").value("John"))
                .andExpect(jsonPath("$.totalElements").value(1));

        verify(contactService, times(1)).getAllContacts(any(PageRequest.class));
    }

    @Test
    void testSyncToElasticsearch() throws Exception {
        // Given
        doNothing().when(contactService).syncAllToElasticsearch();

        // When & Then
        mockMvc.perform(post("/api/v1/contacts/sync"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Sync completed"))
                .andExpect(jsonPath("$.description").value("All contacts have been synchronized to Elasticsearch"));

        verify(contactService, times(1)).syncAllToElasticsearch();
    }

    @Test
    void testCreateContactValidationError() throws Exception {
        // Given
        Contact invalidContact = new Contact();
        // Missing required fields

        // When & Then
        mockMvc.perform(post("/api/v1/contacts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidContact)))
                .andExpect(status().isBadRequest());
    }
} 