package com.saymyname.webapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
        // This test verifies that the Spring Boot application context
        // can start successfully with the MySQL Testcontainer
    }

    @Test
    void removesTemporaryTenantMigrationColumn() {
        Integer columnCount = jdbcTemplate.queryForObject("""
                select count(*)
                  from information_schema.columns
                 where table_schema = database()
                   and table_name = 'tenants'
                   and column_name = 'migration_test_note'
                """, Integer.class);

        assertThat(columnCount).isZero();
    }

    @Test
    void createsAllCriticalTenantScopedForeignKeys() {
        List<ForeignKeyShape> foreignKeys = jdbcTemplate.query("""
                select kcu.table_name,
                       kcu.referenced_table_name,
                       group_concat(kcu.column_name order by kcu.ordinal_position separator ','),
                       group_concat(kcu.referenced_column_name order by kcu.ordinal_position separator ',')
                  from information_schema.key_column_usage kcu
                 where kcu.table_schema = database()
                   and kcu.referenced_table_name is not null
                 group by kcu.table_name, kcu.constraint_name, kcu.referenced_table_name
                """, (rs, rowNum) -> new ForeignKeyShape(
                rs.getString(1), rs.getString(2), rs.getString(3), rs.getString(4)));

        assertThat(foreignKeys).contains(
                fk("change_requests", "attributes",
                        "tenant_id,attribute_id", "tenant_id,id"),

                fk("course_question_attempts", "courses",
                        "tenant_id,course_id", "tenant_id,id"),

                fk("course_question_items", "course_question_attempts",
                        "tenant_id,attempt_id", "tenant_id,id"),

                fk("course_question_items", "persons",
                        "tenant_id,person_id", "tenant_id,id"),

                fk("facts", "attributes",
                        "tenant_id,attribute_id", "tenant_id,id"),

                fk("facts", "persons",
                        "tenant_id,person_id", "tenant_id,id"),

                fk("photos", "persons",
                        "tenant_id,person_id", "tenant_id,id"),

                fk("user_subscriptions", "persons",
                        "tenant_id,person_id", "tenant_id,id"),

                fk("workspace_persons", "persons",
                        "tenant_id,person_id", "tenant_id,id"),

                // Cross-tenant protection for workspace-scoped records
                fk("facts", "workspaces",
                        "tenant_id,workspace_id", "tenant_id,id"),

                fk("workspace_members", "workspaces",
                        "tenant_id,workspace_id", "tenant_id,id"),

                fk("workspace_persons", "workspaces",
                        "tenant_id,workspace_id", "tenant_id,id"));
    }

    private static ForeignKeyShape fk(
            String table, String referencedTable, String columns, String referencedColumns) {
        return new ForeignKeyShape(table, referencedTable, columns, referencedColumns);
    }

    private record ForeignKeyShape(
            String table, String referencedTable, String columns, String referencedColumns) {
    }
}
