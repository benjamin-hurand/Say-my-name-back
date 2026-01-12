package com.saymyname.webapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration test to ensure the Spring Boot application context loads successfully.
 * This is a smoke test that verifies all beans can be created and autowired correctly.
 */
@SpringBootTest
@ActiveProfiles("test")
class WebappApplicationTest {

    @Test
    void contextLoads() {
        // This test verifies that the Spring Boot application context
        // can start successfully without errors
    }
}
