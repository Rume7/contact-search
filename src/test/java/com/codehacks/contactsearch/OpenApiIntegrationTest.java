package com.codehacks.contactsearch;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.springframework.context.annotation.Import;
import com.codehacks.contactsearch.integration.TestSecurityConfig;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.boot.test.mock.mockito.MockBean;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Testcontainers
@ActiveProfiles("integration")
@Import(TestSecurityConfig.class)
@ComponentScan(basePackages = "com.codehacks.contactsearch", 
               excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, 
                                                    pattern = "com\\.codehacks\\.contactsearch\\.(security|service\\.(AuthService|UserService)|controller\\.AuthController)"))
@MockBean(com.codehacks.contactsearch.controller.AuthController.class)
@MockBean(com.codehacks.contactsearch.service.AuthService.class)
@MockBean(com.codehacks.contactsearch.service.UserService.class)
class OpenApiIntegrationTest {

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
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.sql.init.mode", () -> "never");
    }

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Test
    void testOpenApiDocumentationIsAccessible() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Test that OpenAPI JSON is accessible
        mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));

        // Test that Swagger UI is accessible (it redirects to /swagger-ui/index.html)
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void testV1ApiEndpointsAreDocumented() throws Exception {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        // Test that v1 API endpoints are accessible
        mockMvc.perform(get("/api/v1/contacts"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/search/contacts?query=test"))
                .andExpect(status().isOk());
    }
} 