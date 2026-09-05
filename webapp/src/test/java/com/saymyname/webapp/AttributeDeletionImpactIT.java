package com.saymyname.webapp;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.enums.CasingStrategy;
import com.saymyname.core.model.enums.EditPolicy;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.AttributeDeletionImpact;
import com.saymyname.core.model.people.Fact;
import com.saymyname.core.model.people.ValueType;
import com.saymyname.core.multitenancy.TenantContext;
import com.saymyname.service.AttributeService;
import com.saymyname.service.FactService;
import com.saymyname.webapp.config.TestcontainersConfiguration;

/**
 * End-to-end coverage (real MySQL via Testcontainers, migrated V1->latest)
 * for the deletion-impact bulk queries backing the "safe attribute deletion"
 * UX: facts/persons, courses and pending change requests referencing an
 * attribute must each be counted correctly and independently per attribute
 * in a single grouped-by-attribute_id bulk call, matching the canDelete rule.
 */
@SpringBootTest(classes = WebappApplication.class)
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestcontainersConfiguration.Initializer.class)
@Transactional
class AttributeDeletionImpactIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private AttributeService attributeService;
    @Autowired
    private FactService factService;

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void deletionImpactCountsEachRelationIndependentlyPerAttributeInOneBulkCall() {
        Long tenantId = createTenant();
        TenantContext.set(tenantId);
        Long userId = createUser();

        Attribute usedByFacts = attributeService.create(customAttribute("Used By Facts"));
        Attribute usedByCourse = attributeService.create(customAttribute("Used By Course"));
        Attribute usedByPendingRequest = attributeService.create(customAttribute("Used By Pending Request"));
        Attribute unused = attributeService.create(customAttribute("Unused"));

        Long personOne = createPerson(tenantId);
        Long personTwo = createPerson(tenantId);
        setFact(personOne, usedByFacts.getId(), "a");
        setFact(personTwo, usedByFacts.getId(), "b");

        createCourse(tenantId, userId, usedByCourse.getId());

        createPendingChangeRequest(tenantId, personOne, userId, usedByPendingRequest.getId());

        Map<Long, AttributeDeletionImpact> impact = attributeService.getDeletionImpact(
                List.of(usedByFacts, usedByCourse, usedByPendingRequest, unused));

        AttributeDeletionImpact factsImpact = impact.get(usedByFacts.getId());
        assertThat(factsImpact.factCount()).isEqualTo(2L);
        assertThat(factsImpact.personCount()).isEqualTo(2L);
        assertThat(factsImpact.courseCount()).isZero();
        assertThat(factsImpact.pendingChangeRequestCount()).isZero();
        assertThat(factsImpact.canDelete()).isFalse();

        AttributeDeletionImpact courseImpact = impact.get(usedByCourse.getId());
        assertThat(courseImpact.factCount()).isZero();
        assertThat(courseImpact.courseCount()).isEqualTo(1L);
        assertThat(courseImpact.pendingChangeRequestCount()).isZero();
        assertThat(courseImpact.canDelete()).isFalse();

        AttributeDeletionImpact pendingImpact = impact.get(usedByPendingRequest.getId());
        assertThat(pendingImpact.factCount()).isZero();
        assertThat(pendingImpact.courseCount()).isZero();
        assertThat(pendingImpact.pendingChangeRequestCount()).isEqualTo(1L);
        assertThat(pendingImpact.canDelete()).isFalse();

        AttributeDeletionImpact unusedImpact = impact.get(unused.getId());
        assertThat(unusedImpact.factCount()).isZero();
        assertThat(unusedImpact.courseCount()).isZero();
        assertThat(unusedImpact.pendingChangeRequestCount()).isZero();
        assertThat(unusedImpact.canDelete()).isTrue();
    }

    private Attribute customAttribute(String name) {
        return new Attribute.Builder()
                .withName(name)
                .withType(ValueType.TEXT)
                .withMaxValues(1)
                .withEditPolicy(EditPolicy.FREE)
                .withCasingStrategy(CasingStrategy.NONE)
                .build();
    }

    private Long createTenant() {
        jdbcTemplate.update("insert into tenants (kind) values ('ORG')");
        Long tenantId = jdbcTemplate.queryForObject("select last_insert_id()", Long.class);
        jdbcTemplate.update(
                "insert into tenant_orgs (tenant_id, org_key, name) values (?, ?, ?)",
                tenantId, "test-org-" + tenantId, "Test Org " + tenantId);
        return tenantId;
    }

    private Long createPerson(Long tenantId) {
        jdbcTemplate.update("insert into persons (tenant_id) values (?)", tenantId);
        return jdbcTemplate.queryForObject("select last_insert_id()", Long.class);
    }

    private Long createUser() {
        jdbcTemplate.update(
                "insert into users (public_id, display_name) values (RANDOM_BYTES(16), ?)",
                "Test User");
        return jdbcTemplate.queryForObject("select last_insert_id()", Long.class);
    }

    private void createCourse(Long tenantId, Long userId, Long targetAttributeId) {
        jdbcTemplate.update("""
                insert into courses (tenant_id, user_id, target_attribute_id, status, current_round, population_scope)
                values (?, ?, ?, 'IN_PROGRESS', 0, 'ALL')
                """, tenantId, userId, targetAttributeId);
    }

    private void createPendingChangeRequest(Long tenantId, Long personId, Long requesterId, Long attributeId) {
        jdbcTemplate.update("""
                insert into change_requests (tenant_id, person_id, requester_id, attribute_id, request_reason, status, created_at)
                values (?, ?, ?, ?, 'test reason', 'PENDING', now())
                """, tenantId, personId, requesterId, attributeId);
    }

    private void setFact(Long personId, Long attributeId, String value) {
        factService.applyChangesForPerson(personId, attributeId,
                List.of(new Fact.Builder().withValue(value).build()),
                List.of(), List.of(), true);
    }
}
