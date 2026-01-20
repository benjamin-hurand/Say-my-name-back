package com.saymyname.webapp.config;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.lifecycle.Startables;

/**
 * Testcontainers configuration that can be used with @ContextConfiguration.
 * This approach avoids annotation inheritance conflicts.
 */
public class TestcontainersConfiguration {

    private static final MySQLContainer<?> mysql;

    static {
        @SuppressWarnings("resource")
        MySQLContainer<?> container = new MySQLContainer<>("mysql:8.4")
                .withDatabaseName("saymyname_test")
                .withUsername("test")
                .withPassword("test");
        mysql = container;

        Startables.deepStart(mysql).join();
    }

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertyValues.of(
                    // Custom db.* properties
                    "db.driver=" + "com.mysql.cj.jdbc.Driver",
                    "db.url=" + mysql.getJdbcUrl(),
                    "db.username=" + mysql.getUsername(),
                    "db.password=" + mysql.getPassword(),
                    // Standard Spring DataSource properties
                    "spring.datasource.url=" + mysql.getJdbcUrl(),
                    "spring.datasource.username=" + mysql.getUsername(),
                    "spring.datasource.password=" + mysql.getPassword(),
                    "spring.datasource.driver-class-name=" + "com.mysql.cj.jdbc.Driver"
            ).applyTo(applicationContext.getEnvironment());
        }
    }

    public static MySQLContainer<?> getMySqlContainer() {
        return mysql;
    }
}
