package com.codehacks.contactsearch.integration;

import com.codehacks.contactsearch.model.Contact;
import com.codehacks.contactsearch.service.ContactService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Testcontainers
@ActiveProfiles("integration")
@Import(TestSecurityConfig.class)
@ComponentScan(basePackages = "com.codehacks.contactsearch", 
               excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, 
                                                    pattern = "com\\.codehacks\\.contactsearch\\.(security|service\\.(AuthService|UserService)|controller\\.AuthController)"))
@Profile("integration")
@MockBean(com.codehacks.contactsearch.controller.AuthController.class)
@MockBean(com.codehacks.contactsearch.service.AuthService.class)
@MockBean(com.codehacks.contactsearch.service.UserService.class)
class ContactIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16.8-alpine")
            .withDatabaseName("contacts_db_test")
            .withUsername("postgres")
            .withPassword("password")
            .withReuse(true);

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
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.elasticsearch.uris", () -> "http://" + elasticsearch.getHost() + ":" + elasticsearch.getMappedPort(9200));
    }

    @Autowired
    private ContactService contactService;

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        contactService.getAllContacts(org.springframework.data.domain.PageRequest.of(0, 100))
                .getContent()
                .forEach(contact -> contactService.deleteContact(contact.getId()));
    }

    @Test
    void testCreateAndRetrieveContact() {
        // Create a contact
        Contact contact = new Contact();
        contact.setFirstName("John");
        contact.setLastName("Doe");
        contact.setEmail("john.doe@test.com");
        contact.setCity("Test City");

        Contact savedContact = contactService.createContact(contact);

        assertThat(savedContact.getId()).isNotNull();
        assertThat(savedContact.getFirstName()).isEqualTo("John");
        assertThat(savedContact.getLastName()).isEqualTo("Doe");
        assertThat(savedContact.getEmail()).isEqualTo("john.doe@test.com");
        assertThat(savedContact.getCity()).isEqualTo("Test City");
        assertThat(savedContact.getCreatedAt()).isNotNull();
        assertThat(savedContact.getUpdatedAt()).isNotNull();
    }

    @Test
    void testContactApiEndpoints() {
        // Test POST /api/v1/contacts
        Contact contact = new Contact();
        contact.setFirstName("Jane");
        contact.setLastName("Smith");
        contact.setEmail("jane.smith@test.com");
        contact.setCity("Test City");

        ResponseEntity<Contact> createResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/contacts",
                contact,
                Contact.class
        );

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(createResponse.getBody()).isNotNull();
        assertThat(createResponse.getBody().getId()).isNotNull();

        // Test GET /api/v1/contacts
        ResponseEntity<String> getResponse = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/v1/contacts",
                String.class
        );

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).contains("Jane");
        assertThat(getResponse.getBody()).contains("Smith");

        // Test GET /api/v1/contacts/{id}
        Long contactId = createResponse.getBody().getId();
        ResponseEntity<Contact> getByIdResponse = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/v1/contacts/" + contactId,
                Contact.class
        );

        assertThat(getByIdResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getByIdResponse.getBody()).isNotNull();
        assertThat(getByIdResponse.getBody().getFirstName()).isEqualTo("Jane");
    }

    @Test
    void testSearchFunctionality() {
        // Create test contacts
        Contact contact1 = new Contact();
        contact1.setFirstName("Alice");
        contact1.setLastName("Johnson");
        contact1.setEmail("alice.johnson@test.com");
        contact1.setCity("New York");

        Contact contact2 = new Contact();
        contact2.setFirstName("Bob");
        contact2.setLastName("Williams");
        contact2.setEmail("bob.williams@test.com");
        contact2.setCity("Los Angeles");

        contactService.createContact(contact1);
        contactService.createContact(contact2);

        // Sync to Elasticsearch
        ResponseEntity<String> syncResponse = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/contacts/sync",
                null,
                String.class
        );

        assertThat(syncResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Test search endpoint
        ResponseEntity<String> searchResponse = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/v1/search/contacts?query=alice",
                String.class
        );

        assertThat(searchResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(searchResponse.getBody()).contains("Alice");
        assertThat(searchResponse.getBody()).contains("Johnson");
    }

    @Test
    void testHealthEndpoint() {
        ResponseEntity<String> healthResponse = restTemplate.getForEntity(
                "http://localhost:" + port + "/actuator/health",
                String.class
        );

        assertThat(healthResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(healthResponse.getBody()).contains("UP");
    }
} 