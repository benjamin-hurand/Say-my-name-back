package com.saymyname.webapp;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractMySqlIT {

    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("saymyname_test")
            .withUsername("test")
            .withPassword("test");

    static {
        MYSQL.start();
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry r) {
        r.add("db.host", MYSQL::getHost);
        r.add("db.port", () -> MYSQL.getMappedPort(3306));
        r.add("db.name", MYSQL::getDatabaseName);
        r.add("db.username", MYSQL::getUsername);
        r.add("db.password", MYSQL::getPassword);

        // Si ton DataSourceConfig lit spring.datasource.* au lieu de db.*
        r.add("spring.datasource.url", MYSQL::getJdbcUrl);
        r.add("spring.datasource.username", MYSQL::getUsername);
        r.add("spring.datasource.password", MYSQL::getPassword);
    }
}
