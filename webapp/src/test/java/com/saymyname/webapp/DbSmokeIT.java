package com.saymyname.webapp;

import com.saymyname.persistence.entity.organization.OrganizationEntity;
import com.saymyname.webapp.config.TestcontainersConfiguration;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Database smoke test to verify basic CRUD operations with MySQL Testcontainer.
 */
@SpringBootTest(classes = WebappApplication.class)
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestcontainersConfiguration.Initializer.class)
class DbSmokeIT {

    @Autowired
    EntityManager em;

    @Test
    @Transactional
    void should_insert_and_read_organization() {
        // 1) insert
        OrganizationEntity org = new OrganizationEntity();
        org.setKey("test-org-" + System.nanoTime());
        org.setName("Org Test");
        org.setActive(true);

        em.persist(org);
        em.flush();

        Long id = org.getId();
        assertThat(id).isNotNull();

        // 2) read (force read from DB)
        em.clear();
        OrganizationEntity reloaded = em.find(OrganizationEntity.class, id);

        assertThat(reloaded).isNotNull();
        assertThat(reloaded.getKey()).startsWith("test-org-");
        assertThat(reloaded.getName()).isEqualTo("Org Test");
        assertThat(reloaded.isActive()).isTrue();
    }
}
