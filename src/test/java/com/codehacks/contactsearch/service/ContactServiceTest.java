package com.codehacks.contactsearch.service;

import com.codehacks.contactsearch.model.Contact;
import com.codehacks.contactsearch.integration.TestSecurityConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("integration")
@Import(TestSecurityConfig.class)
@ComponentScan(basePackages = "com.codehacks.contactsearch", 
               excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, 
                                                    pattern = "com\\.codehacks\\.contactsearch\\.(security|service\\.(AuthService|UserService)|controller\\.AuthController)"))
@MockBean(com.codehacks.contactsearch.controller.AuthController.class)
@MockBean(com.codehacks.contactsearch.service.AuthService.class)
@MockBean(com.codehacks.contactsearch.service.UserService.class)
class ContactServiceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.8-alpine")
            .withDatabaseName("contacts_db_test")
            .withUsername("postgres")
            .withPassword("password");

    @Container
    static GenericContainer<?> elasticsearch = new GenericContainer<>(DockerImageName.parse("docker.elastic.co/elasticsearch/elasticsearch:8.8.0"))
            .withEnv("discovery.type", "single-node")
            .withEnv("xpack.security.enabled", "false")
            .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m")
            .withExposedPorts(9200);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.elasticsearch.uris", () -> "http://" + elasticsearch.getHost() + ":" + elasticsearch.getMappedPort(9200));
    }

    @Autowired
    private ContactService contactService;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        Page<Contact> contacts = contactService.getAllContacts(PageRequest.of(0, 100));
        contacts.getContent().forEach(contact -> contactService.deleteContact(contact.getId()));
    }

    @Test
    void testCreateContact() {
        // Given
        Contact contact = new Contact();
        contact.setFirstName("John");
        contact.setLastName("Doe");
        contact.setEmail("john.doe@test.com");
        contact.setCity("Test City");

        // When
        Contact savedContact = contactService.createContact(contact);

        // Then
        assertThat(savedContact.getId()).isNotNull();
        assertThat(savedContact.getFirstName()).isEqualTo("John");
        assertThat(savedContact.getLastName()).isEqualTo("Doe");
        assertThat(savedContact.getEmail()).isEqualTo("john.doe@test.com");
        assertThat(savedContact.getCity()).isEqualTo("Test City");
        assertThat(savedContact.getCreatedAt()).isNotNull();
        assertThat(savedContact.getUpdatedAt()).isNotNull();
    }

    @Test
    void testGetContactById() {
        // Given
        Contact contact = new Contact();
        contact.setFirstName("Jane");
        contact.setLastName("Smith");
        contact.setEmail("jane.smith@test.com");
        contact.setCity("Test City");

        Contact savedContact = contactService.createContact(contact);

        // When
        Optional<Contact> foundContact = contactService.getContactById(savedContact.getId());

        // Then
        assertThat(foundContact).isPresent();
        assertThat(foundContact.get().getFirstName()).isEqualTo("Jane");
        assertThat(foundContact.get().getLastName()).isEqualTo("Smith");
        assertThat(foundContact.get().getEmail()).isEqualTo("jane.smith@test.com");
    }

    @Test
    void testUpdateContact() {
        // Given
        Contact contact = new Contact();
        contact.setFirstName("Alice");
        contact.setLastName("Johnson");
        contact.setEmail("alice.johnson@test.com");
        contact.setCity("Old City");

        Contact savedContact = contactService.createContact(contact);

        // When
        Contact updatedContact = new Contact();
        updatedContact.setFirstName("Alice");
        updatedContact.setLastName("Johnson");
        updatedContact.setEmail("alice.johnson@test.com");
        updatedContact.setCity("New City");

        Contact result = contactService.updateContact(savedContact.getId(), updatedContact);

        // Then
        assertThat(result.getCity()).isEqualTo("New City");
        assertThat(result.getUpdatedAt()).isAfter(savedContact.getCreatedAt());
    }

    @Test
    void testDeleteContact() {
        // Given
        Contact contact = new Contact();
        contact.setFirstName("Bob");
        contact.setLastName("Williams");
        contact.setEmail("bob.williams@test.com");
        contact.setCity("Test City");

        Contact savedContact = contactService.createContact(contact);

        // When
        contactService.deleteContact(savedContact.getId());

        // Then
        Optional<Contact> foundContact = contactService.getContactById(savedContact.getId());
        assertThat(foundContact).isEmpty();
    }

    @Test
    void testGetAllContacts() {
        // Given
        Contact contact1 = new Contact();
        contact1.setFirstName("Alice");
        contact1.setLastName("Johnson");
        contact1.setEmail("alice.johnson@test.com");
        contact1.setCity("City A");

        Contact contact2 = new Contact();
        contact2.setFirstName("Bob");
        contact2.setLastName("Williams");
        contact2.setEmail("bob.williams@test.com");
        contact2.setCity("City B");

        contactService.createContact(contact1);
        contactService.createContact(contact2);

        // When
        Page<Contact> contacts = contactService.getAllContacts(PageRequest.of(0, 10));

        // Then
        assertThat(contacts.getContent()).hasSize(2);
        assertThat(contacts.getTotalElements()).isEqualTo(2);
    }

    @Test
    void testSyncToElasticsearch() {
        // Given
        Contact contact = new Contact();
        contact.setFirstName("Charlie");
        contact.setLastName("Brown");
        contact.setEmail("charlie.brown@test.com");
        contact.setCity("Test City");

        contactService.createContact(contact);

        // When
        contactService.syncAllToElasticsearch();

        // Then - no exception should be thrown
        // The sync method should complete successfully
        assertThat(true).isTrue(); // Placeholder assertion
    }
} 