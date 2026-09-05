package com.saymyname.service.person;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.ValueType;
import com.saymyname.core.model.persondirectory.AdminPersonSearchCriteria;
import com.saymyname.core.model.persondirectory.PersonSearchCriteria;
import com.saymyname.core.multitenancy.TenantContext;
import com.saymyname.persistence.dao.AttributeDao;
import com.saymyname.service.config.BaseServiceTest;

class PersonSearchAttributeGuardTest extends BaseServiceTest {

    @Mock
    private AttributeDao attributeDao;

    @InjectMocks
    private PersonSearchAttributeGuard guard;

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void allowsFilteringByFilterableAttribute() {
        TenantContext.set(1L);
        stubAttribute(7L, ValueType.NUMBER);

        var criteria = new PersonSearchCriteria.Builder()
                .withFilters(List.of(new PersonSearchCriteria.AttributeFilter.Builder()
                        .withAttributeId(7L).withOperator("RANGE").withValues(List.of("1", "10")).build()))
                .build();

        assertThatCode(() -> guard.validate(criteria)).doesNotThrowAnyException();
    }

    @Test
    void rejectsFilteringByNonFilterableAttribute() {
        TenantContext.set(1L);
        stubAttribute(7L, ValueType.TEXT);

        var criteria = new PersonSearchCriteria.Builder()
                .withFilters(List.of(new PersonSearchCriteria.AttributeFilter.Builder()
                        .withAttributeId(7L).withOperator("IN").withValues(List.of("a")).build()))
                .build();

        assertThatThrownBy(() -> guard.validate(criteria))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void rejectsSortingByNonSortableAttribute() {
        TenantContext.set(1L);
        stubAttribute(9L, ValueType.TEXT);

        var criteria = new PersonSearchCriteria.Builder()
                .withSort(List.of(new PersonSearchCriteria.SortDirective.Builder()
                        .withKind("ATTRIBUTE").withAttributeId(9L).withDirection("ASC").build()))
                .build();

        assertThatThrownBy(() -> guard.validate(criteria))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void allowsSortingBySortableAttribute() {
        TenantContext.set(1L);
        stubAttribute(9L, ValueType.ENUM);

        var criteria = new PersonSearchCriteria.Builder()
                .withSort(List.of(new PersonSearchCriteria.SortDirective.Builder()
                        .withKind("ATTRIBUTE").withAttributeId(9L).withDirection("ASC").build()))
                .build();

        assertThatCode(() -> guard.validate(criteria)).doesNotThrowAnyException();
    }

    @Test
    void neverRejectsTheGlobalTextSearchPseudoAttribute() {
        var criteria = new PersonSearchCriteria.Builder()
                .withFilters(List.of(new PersonSearchCriteria.AttributeFilter.Builder()
                        .withAttributeId(-1L).withOperator("LIKE").withValues(List.of("anna")).build()))
                .build();

        assertThatCode(() -> guard.validate(criteria)).doesNotThrowAnyException();
    }

    @Test
    void rejectsUnknownAttributeId() {
        TenantContext.set(1L);
        when(attributeDao.findAllByIdsForTenant(any(), any())).thenReturn(List.of());

        var criteria = new PersonSearchCriteria.Builder()
                .withFilters(List.of(new PersonSearchCriteria.AttributeFilter.Builder()
                        .withAttributeId(404L).withOperator("IN").withValues(List.of("x")).build()))
                .build();

        assertThatThrownBy(() -> guard.validate(criteria))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode())
                        .isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void validatesAdminCriteriaTheSameWay() {
        TenantContext.set(1L);
        stubAttribute(7L, ValueType.TEXT);

        var criteria = new AdminPersonSearchCriteria.Builder()
                .withFilters(List.of(new AdminPersonSearchCriteria.AttributeFilter.Builder()
                        .withAttributeId(7L).withOperator("LIKE").withValues(List.of("a")).build()))
                .build();

        assertThatThrownBy(() -> guard.validateAdmin(criteria))
                .isInstanceOf(ResponseStatusException.class);
    }

    private void stubAttribute(Long id, ValueType type) {
        Attribute attribute = new Attribute.Builder()
                .withId(id)
                .withName("Attr " + id)
                .withType(type)
                .withMaxValues(1)
                .build();
        when(attributeDao.findAllByIdsForTenant(anyLong(), any())).thenReturn(List.of(attribute));
    }
}
