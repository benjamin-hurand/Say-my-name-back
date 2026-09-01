package com.saymyname.service;

import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.Concept;
import com.saymyname.core.model.people.ValueType;
import com.saymyname.core.multitenancy.TenantContext;
import com.saymyname.persistence.dao.AttributeDao;
import com.saymyname.persistence.dao.ConceptDao;
import com.saymyname.service.config.BaseServiceTest;
import com.saymyname.service.attribute.AttributeMetaCache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AttributeServiceTest extends BaseServiceTest {

    @Mock
    private AttributeDao attributeDao;

    @Mock
    private ConceptDao conceptDao;

    @Mock
    private AttributeMetaCache attributeMetaCache;

    @InjectMocks
    private AttributeService service;

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void rejectsSecondAttributeWithSameConceptInSameTenant() {
        TenantContext.set(10L);
        Attribute first = attribute(null, 5L);
        Attribute second = attribute(null, 5L);
        stubConcept(5L);
        when(attributeDao.save(any(Attribute.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(attributeDao.existsOtherByTenantIdAndConceptId(10L, 5L, null))
                .thenReturn(false, true);

        assertThatCode(() -> service.create(first)).doesNotThrowAnyException();
        assertThatThrownBy(() -> service.create(second))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    var responseError = (ResponseStatusException) error;
                    assertThat(responseError.getStatusCode().value()).isEqualTo(409);
                    assertThat(responseError.getReason())
                            .isEqualTo("Ce concept est déjà utilisé par un attribut de ce tenant");
                });
    }

    @Test
    void allowsSameConceptInDifferentTenants() {
        stubConcept(5L);
        when(attributeDao.save(any(Attribute.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(attributeDao.existsOtherByTenantIdAndConceptId(10L, 5L, null)).thenReturn(false);
        when(attributeDao.existsOtherByTenantIdAndConceptId(20L, 5L, null)).thenReturn(false);

        TenantContext.set(10L);
        assertThatCode(() -> service.create(attribute(null, 5L))).doesNotThrowAnyException();
        TenantContext.set(20L);
        assertThatCode(() -> service.create(attribute(null, 5L))).doesNotThrowAnyException();

        verify(attributeDao).existsOtherByTenantIdAndConceptId(10L, 5L, null);
        verify(attributeDao).existsOtherByTenantIdAndConceptId(20L, 5L, null);
    }

    @Test
    void allowsMultipleCustomAttributesWithoutConcept() {
        when(attributeDao.save(any(Attribute.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThatCode(() -> service.create(attribute(null, null))).doesNotThrowAnyException();
        assertThatCode(() -> service.create(attribute(null, null))).doesNotThrowAnyException();

        verify(conceptDao, never()).findById(any());
        verify(attributeDao, never()).existsOtherByTenantIdAndConceptId(any(), any(), any());
    }

    @Test
    void updateExcludesCurrentAttributeFromDuplicateCheck() {
        TenantContext.set(10L);
        stubConcept(5L);
        when(attributeDao.findById(42L)).thenReturn(Optional.of(attribute(42L, 5L)));
        when(attributeDao.existsOtherByTenantIdAndConceptId(10L, 5L, 42L)).thenReturn(false);
        when(attributeDao.save(any(Attribute.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThatCode(() -> service.update(attribute(42L, 5L))).doesNotThrowAnyException();

        verify(attributeDao).existsOtherByTenantIdAndConceptId(10L, 5L, 42L);
    }

    @Test
    void rejectsUpdateToConceptUsedByAnotherAttribute() {
        TenantContext.set(10L);
        stubConcept(5L);
        when(attributeDao.findById(42L)).thenReturn(Optional.of(attribute(42L, 5L)));
        when(attributeDao.existsOtherByTenantIdAndConceptId(10L, 5L, 42L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(attribute(42L, 5L)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> {
                    var responseError = (ResponseStatusException) error;
                    assertThat(responseError.getStatusCode().value()).isEqualTo(409);
                    assertThat(responseError.getReason())
                            .isEqualTo("Ce concept est déjà utilisé par un attribut de ce tenant");
                });

        verify(attributeDao, never()).save(any(Attribute.class));
    }

    @Test
    void rejectsNormalUpdateOfIdentitySystemAttribute() {
        Attribute current = attribute(42L, 9L);
        current.setConceptCode("IDENTITY");
        when(attributeDao.findById(42L)).thenReturn(Optional.of(current));

        assertThatThrownBy(() -> service.update(current))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode().value())
                        .isEqualTo(403));

        verify(attributeDao, never()).save(any(Attribute.class));
    }

    private void stubConcept(Long conceptId) {
        Concept concept = new Concept.Builder()
                .withId(conceptId)
                .withValueType(ValueType.TEXT)
                .build();
        when(conceptDao.findById(conceptId)).thenReturn(Optional.of(concept));
    }

    private Attribute attribute(Long id, Long conceptId) {
        return new Attribute.Builder()
                .withId(id)
                .withConceptId(conceptId)
                .withType(ValueType.TEXT)
                .withMaxValues(1)
                .build();
    }
}
