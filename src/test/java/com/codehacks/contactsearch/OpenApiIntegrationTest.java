package com.codehacks.contactsearch;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureWebMvc
@TestPropertySource(properties = {
    "spring.elasticsearch.uris=http://localhost:9200",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=never"
})
class OpenApiIntegrationTest {

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

        // Test that Swagger UI is accessible
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isOk());
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