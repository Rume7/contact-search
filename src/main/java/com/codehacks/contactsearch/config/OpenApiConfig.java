package com.codehacks.contactsearch.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Contact Search API v1")
                        .description("A Spring Boot application for managing and searching contacts with PostgreSQL and Elasticsearch integration. This is version 1 of the API.")
                        .version("1.0.3")
                        .contact(new Contact()
                                .name("Contact Search Team")
                                .email("support@contactsearch.com")
                                .url("https://github.com/Rume7/contact-search"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development Server"),
                        new Server()
                                .url("https://api.contactsearch.com")
                                .description("Production Server")
                ));
    }
} 