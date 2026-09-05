package com.saymyname.webapp;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.model.enums.CasingStrategy;
import com.saymyname.core.model.enums.EditPolicy;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.Fact;
import com.saymyname.core.model.people.ValueType;
import com.saymyname.core.model.persondirectory.PersonCard;
import com.saymyname.core.model.persondirectory.PersonSearchCriteria;
import com.saymyname.core.multitenancy.TenantContext;
import com.saymyname.service.AttributeService;
import com.saymyname.service.FactService;
import com.saymyname.service.person.PersonService;
import com.saymyname.webapp.config.TestcontainersConfiguration;

/**
 * End-to-end coverage (real MySQL via Testcontainers, migrated V1->latest)
 * for the auto-derived filter/sort capabilities: NUMBER sorts numerically
 * (not lexically), ENUM sorts by admin-configured order_index (not
 * alphabetically), and /persons/search refuses to filter/sort by a
 * non-filterable/non-sortable attribute (TEXT).
 */
@SpringBootTest(classes = WebappApplication.class)
@ActiveProfiles("test")
@ContextConfiguration(initializers = TestcontainersConfiguration.Initializer.class)
@Transactional
class PersonSearchCapabilitiesIT {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private AttributeService attributeService;
    @Autowired
    private FactService factService;
    @Autowired
    private PersonService personService;

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void numberSortOrdersNumericallyNotLexically() {
        Long tenantId = createTenant();
        TenantContext.set(tenantId);

        Attribute score = attributeService.create(customAttribute("Score", ValueType.NUMBER));

        Long personTen = createPerson(tenantId);
        setFact(personTen, score.getId(), "10");
        Long personTwo = createPerson(tenantId);
        setFact(personTwo, score.getId(), "2");
        Long personHundred = createPerson(tenantId);
        setFact(personHundred, score.getId(), "100");

        PersonSearchCriteria criteria = new PersonSearchCriteria.Builder()
                .withSort(List.of(new PersonSearchCriteria.SortDirective.Builder()
                        .withKind("ATTRIBUTE").withAttributeId(score.getId()).withDirection("ASC").build()))
                .build();

        List<Long> ids = personService.searchPersons(criteria, PageRequest.of(0, 10), null)
                .getContent().stream().map(PersonCard::getIdPerson).toList();

        assertThat(ids).containsExactly(personTwo, personTen, personHundred);
    }

    @Test
    void enumSortUsesOrderIndexNotAlphabeticalCode() {
        Long tenantId = createTenant();
        TenantContext.set(tenantId);

        // "Zulu" is listed first -> order_index 0; "Alpha" second -> order_index 1.
        // Alphabetically Alpha < Zulu, so a correct order_index-based ascending
        // sort must place the Zulu person BEFORE the Alpha person.
        Attribute priority = attributeService.create(
                customAttribute("Priority", ValueType.ENUM), List.of("Zulu", "Alpha"));

        Long personAlpha = createPerson(tenantId);
        setFact(personAlpha, priority.getId(), "Alpha");
        Long personZulu = createPerson(tenantId);
        setFact(personZulu, priority.getId(), "Zulu");

        PersonSearchCriteria criteria = new PersonSearchCriteria.Builder()
                .withSort(List.of(new PersonSearchCriteria.SortDirective.Builder()
                        .withKind("ATTRIBUTE").withAttributeId(priority.getId()).withDirection("ASC").build()))
                .build();

        List<Long> ids = personService.searchPersons(criteria, PageRequest.of(0, 10), null)
                .getContent().stream().map(PersonCard::getIdPerson).toList();

        assertThat(ids).containsExactly(personZulu, personAlpha);
    }

    @Test
    void searchRejectsSortByNonSortableTextAttribute() {
        Long tenantId = createTenant();
        TenantContext.set(tenantId);

        Attribute note = attributeService.create(customAttribute("Note", ValueType.TEXT));

        PersonSearchCriteria criteria = new PersonSearchCriteria.Builder()
                .withSort(List.of(new PersonSearchCriteria.SortDirective.Builder()
                        .withKind("ATTRIBUTE").withAttributeId(note.getId()).withDirection("ASC").build()))
                .build();

        assertThatThrownBy(() -> personService.searchPersons(criteria, PageRequest.of(0, 10), null))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void searchRejectsFilterByNonFilterableTextAttribute() {
        Long tenantId = createTenant();
        TenantContext.set(tenantId);

        Attribute note = attributeService.create(customAttribute("Note", ValueType.TEXT));

        PersonSearchCriteria criteria = new PersonSearchCriteria.Builder()
                .withFilters(List.of(new PersonSearchCriteria.AttributeFilter.Builder()
                        .withAttributeId(note.getId()).withOperator("IN").withValues(List.of("x")).build()))
                .build();

        assertThatThrownBy(() -> personService.searchPersons(criteria, PageRequest.of(0, 10), null))
                .isInstanceOf(ResponseStatusException.class);
    }

    private Attribute customAttribute(String name, ValueType type) {
        return new Attribute.Builder()
                .withName(name)
                .withType(type)
                .withMaxValues(1)
                .withEditPolicy(EditPolicy.FREE)
                .withCasingStrategy(CasingStrategy.NONE)
                .build();
    }

    private Long createTenant() {
        jdbcTemplate.update("insert into tenants (kind) values ('ORG')");
        return jdbcTemplate.queryForObject("select last_insert_id()", Long.class);
    }

    private Long createPerson(Long tenantId) {
        jdbcTemplate.update("insert into persons (tenant_id) values (?)", tenantId);
        return jdbcTemplate.queryForObject("select last_insert_id()", Long.class);
    }

    private void setFact(Long personId, Long attributeId, String value) {
        factService.applyChangesForPerson(personId, attributeId,
                List.of(new Fact.Builder().withValue(value).build()),
                List.of(), List.of(), true);
    }
}
