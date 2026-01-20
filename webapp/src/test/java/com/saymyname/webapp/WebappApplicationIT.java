package com.saymyname.webapp;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import com.saymyname.webapp.config.TestcontainersConfiguration;

/**
 * Integration test to ensure the Spring Boot application context loads
 * successfully with a real MySQL database (Testcontainers).
 * This is a smoke test that verifies all beans can be created and autowired
 * correctly.
 */
@SpringBootTest(classes = WebappApplication.class)
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestcontainersConfiguration.Initializer.class)
class WebappApplicationIT {

    @Test
    void contextLoads() {
        // This test verifies that the Spring Boot application context
        // can start successfully with the MySQL Testcontainer
    }
}