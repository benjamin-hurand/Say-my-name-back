package com.saymyname.webapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.model.enums.CasingStrategy;
import com.saymyname.core.model.enums.EditPolicy;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.Fact;
import com.saymyname.core.model.people.GenderOptions;
import com.saymyname.core.model.people.ValueType;
import com.saymyname.core.multitenancy.TenantContext;
import com.saymyname.persistence.dao.ConceptDao;
import com.saymyname.service.AttributeService;
import com.saymyname.service.FactService;
import com.saymyname.webapp.config.TestcontainersConfiguration;

/**
 * End-to-end coverage (real MySQL via Testcontainers, migrated V1->latest)
 * for the GENDER canonicalization: system-managed enum option provisioning
 * and Fact validation against active codes.
 */
@SpringBootTest(classes = WebappApplication.class)
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestcontainersConfiguration.Initializer.class)
@Transactional
class GenderCanonicalIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private AttributeService attributeService;
    @Autowired
    private FactService factService;
    @Autowired
    private ConceptDao conceptDao;

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void provisioningGenderAttributeCreatesStableSystemCodesAndValidatesFacts() {
        jdbcTemplate.update("insert into tenants (kind) values ('ORG')");
        Long tenantId = jdbcTemplate.queryForObject("select last_insert_id()", Long.class);
        jdbcTemplate.update("insert into persons (tenant_id) values (?)", tenantId);
        Long personId = jdbcTemplate.queryForObject("select last_insert_id()", Long.class);

        TenantContext.set(tenantId);

        Long genderConceptId = conceptDao.findByCode("GENDER").orElseThrow().getId();
        Attribute gender = new Attribute.Builder()
                .withConceptId(genderConceptId)
                .withName("Genre")
                .withType(ValueType.ENUM)
                .withMaxValues(1)
                .withEditPolicy(EditPolicy.FREE)
                .withCasingStrategy(CasingStrategy.NONE)
                .build();

        // The client attempts to send its own custom option set — it must be
        // silently ignored and replaced by the backend-owned system codes.
        Attribute saved = attributeService.create(gender, List.of("Bleu", "Vert", "Rouge"));

        List<Map<String, Object>> options = jdbcTemplate.queryForList(
                "select code, label, active from attribute_enum_options where attribute_id = ? order by order_index",
                saved.getId());

        assertThat(options).extracting(o -> o.get("code"))
                .containsExactly(GenderOptions.MALE, GenderOptions.FEMALE, GenderOptions.OTHER);
        assertThat(options).allSatisfy(o -> assertThat(o.get("active")).isIn(true, (byte) 1, 1));

        // A Fact using the stable code is accepted...
        factService.applyChangesForPerson(personId, saved.getId(),
                List.of(new Fact.Builder().withValue(GenderOptions.MALE).build()),
                List.of(), List.of(), true);

        String stored = jdbcTemplate.queryForObject(
                "select value from facts where person_id = ? and attribute_id = ? and is_deleted = 0",
                String.class, personId, saved.getId());
        assertThat(stored).isEqualTo(GenderOptions.MALE);

        // ...but the pre-refactor label-shaped value is rejected: it is not one of
        // this attribute's active codes.
        assertThatThrownBy(() -> factService.applyChangesForPerson(personId, saved.getId(),
                List.of(new Fact.Builder().withValue("Homme").build()),
                List.of(), List.of(), true))
                .isInstanceOf(ResponseStatusException.class);
    }
}
