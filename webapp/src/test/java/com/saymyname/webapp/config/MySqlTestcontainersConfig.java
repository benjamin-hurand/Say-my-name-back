package com.saymyname.webapp.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Starts a MySQL container for tests and wires it into the app using custom
 * db.* properties.
 * This is used by the "test" Spring profile (same as before), but DB is now
 * MySQL (Testcontainers).
 */
@Testcontainers
public abstract class MySqlTestcontainersConfig {

    /**
     * Pick a MySQL version close to your prod.
     * You declared mysql.version=8.3.0; container tags available may differ.
     * 8.0 or 8.4 are safe choices.
     */
    @Container
    @SuppressWarnings("resource")
    public static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.4")
            .withDatabaseName("saymyname_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry r) {
        // Your custom DataSourceConfig reads db.* (not spring.datasource.*)
        r.add("db.driver", () -> "com.mysql.cj.jdbc.Driver");
        r.add("db.url", MYSQL::getJdbcUrl);
        r.add("db.username", MYSQL::getUsername);
        r.add("db.password", MYSQL::getPassword);

        // Optional: if someday you migrate to Spring standard props
        r.add("spring.datasource.url", MYSQL::getJdbcUrl);
        r.add("spring.datasource.username", MYSQL::getUsername);
        r.add("spring.datasource.password", MYSQL::getPassword);
        r.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");
    }
}
