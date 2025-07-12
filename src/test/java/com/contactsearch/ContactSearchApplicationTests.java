package com.contactsearch;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class ContactSearchApplicationTests {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
        // This test verifies that the application context loads successfully
        // with the READ COMMITTED isolation level configuration
    }

    @Test
    void testTransactionIsolationLevel() {
        // Verify that the transaction isolation level is set to READ COMMITTED
        String isolationLevel = jdbcTemplate.queryForObject(
            "SHOW transaction_isolation", String.class);
        
        // PostgreSQL returns "read committed" in lowercase
        assertEquals("read committed", isolationLevel.toLowerCase());
    }
} 