package com.saymyname.service;

import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.Concept;
import com.saymyname.core.model.people.ConceptCodes;
import com.saymyname.core.model.people.GenderOptions;
import com.saymyname.core.model.people.ValueType;
import com.saymyname.core.multitenancy.TenantContext;
import com.saymyname.persistence.dao.AttributeDao;
import com.saymyname.persistence.dao.ConceptDao;
import com.saymyname.service.config.BaseServiceTest;
import com.saymyname.service.attribute.AttributeMetaCache;
import com.saymyname.service.identity.IdentityService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AttributeServiceTest extends BaseServiceTest {

    @Mock
    private AttributeDao attributeDao;

    @Mock
    private ConceptDao conceptDao;

    @Mock
    private AttributeMetaCache attributeMetaCache;

    @Mock
    private AttributeEnumOptionService attributeEnumOptionService;

    @Mock
    private IdentityService identityService;

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
        TenantContext.set(10L);
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

    @Test
    void rejectsManualCreationOfIdentitySystemAttribute() {
        TenantContext.set(10L);
        Concept identity = new Concept.Builder()
                .withId(9L)
                .withCode("IDENTITY")
                .withValueType(ValueType.TEXT)
                .build();
        when(conceptDao.findById(9L)).thenReturn(Optional.of(identity));

        assertThatThrownBy(() -> service.create(attribute(null, 9L)))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode().value())
                        .isEqualTo(403));

        verify(attributeDao, never()).save(any(Attribute.class));
    }

    @Test
    void updatesNormalAttributeAndItsEnumOptions() {
        TenantContext.set(10L);
        Attribute current = attribute(42L, null);
        Attribute updated = attribute(42L, null);
        updated.setType(ValueType.ENUM);
        when(attributeDao.findById(42L)).thenReturn(Optional.of(current));
        when(attributeDao.save(updated)).thenReturn(updated);

        assertThat(service.update(updated, List.of("Marketing", "Produit"))).isSameAs(updated);

        verify(attributeEnumOptionService).replaceActiveOptions(42L, List.of("Marketing", "Produit"));
        verify(attributeMetaCache).evictCurrentTenant();
    }

    @Test
    void gendersConceptAttributeIgnoresClientEnumOptionsAndUsesSystemSet() {
        TenantContext.set(10L);
        Attribute created = attribute(null, 5L);
        created.setType(ValueType.ENUM);
        stubGenderConcept(5L);
        when(attributeDao.save(any(Attribute.class))).thenReturn(created);

        service.create(created, List.of("Bleu", "Vert", "Rouge"));

        verify(attributeEnumOptionService).synchronizeSystemOptions(created.getId(), GenderOptions.SYSTEM_OPTIONS);
        verify(attributeEnumOptionService, never()).replaceActiveOptions(any(), anyList());
    }

    @Test
    void gendersConceptAttributeSynchronizesSystemOptionsEvenWithoutClientPayload() {
        TenantContext.set(10L);
        Attribute created = attribute(null, 5L);
        created.setType(ValueType.ENUM);
        stubGenderConcept(5L);
        when(attributeDao.save(any(Attribute.class))).thenReturn(created);

        service.create(created, null);

        verify(attributeEnumOptionService).synchronizeSystemOptions(created.getId(), GenderOptions.SYSTEM_OPTIONS);
    }

    @Test
    void assigningFirstNameConceptForcesIdentitySourceAndRecomposesAllIdentities() {
        TenantContext.set(10L);
        Attribute current = attribute(42L, null);
        Attribute updated = attribute(42L, 5L);
        stubConcept(5L, ConceptCodes.FIRST_NAME, true);
        when(attributeDao.findById(42L)).thenReturn(Optional.of(current));
        when(attributeDao.existsOtherByTenantIdAndConceptId(10L, 5L, 42L)).thenReturn(false);
        when(attributeDao.save(any(Attribute.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Attribute saved = service.update(updated);

        assertThat(saved.isIdentitySource()).isTrue();
        verify(identityService).synchronizeAllCurrentTenant(any());
    }

    @Test
    void removingFirstNameConceptForcesIdentitySourceOffAndRecomposesAllIdentities() {
        TenantContext.set(10L);
        Attribute current = attribute(42L, 5L);
        current.setIdentitySource(true);
        Attribute updated = attribute(42L, null);
        when(attributeDao.findById(42L)).thenReturn(Optional.of(current));
        when(attributeDao.save(any(Attribute.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Attribute saved = service.update(updated);

        assertThat(saved.isIdentitySource()).isFalse();
        verify(identityService).synchronizeAllCurrentTenant(any());
    }

    @Test
    void ignoresClientSuppliedIdentitySourceOnCustomAttribute() {
        // MVP: the admin no longer chooses identitySource — the client value is
        // never honored, even for a custom attribute that historically had it set.
        TenantContext.set(10L);
        Attribute current = attribute(42L, null);
        Attribute updated = attribute(42L, null);
        updated.setIdentitySource(true);
        when(attributeDao.findById(42L)).thenReturn(Optional.of(current));
        when(attributeDao.save(any(Attribute.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Attribute saved = service.update(updated);

        assertThat(saved.isIdentitySource()).isFalse();
        verify(identityService, never()).synchronizeAllCurrentTenant(any());
    }

    @Test
    void deletesNormalAttributeAndEvictsCache() {
        Attribute current = attribute(42L, null);
        when(attributeDao.findById(42L)).thenReturn(Optional.of(current));

        service.delete(42L);

        verify(attributeDao).delete(42L);
        verify(attributeMetaCache).evictCurrentTenant();
    }

    @Test
    void rejectsDeletionWhenAttributeIsStillReferenced() {
        Attribute current = attribute(42L, null);
        when(attributeDao.findById(42L)).thenReturn(Optional.of(current));
        doThrow(new DataIntegrityViolationException("referenced"))
                .when(attributeDao).delete(42L);

        assertThatThrownBy(() -> service.delete(42L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode().value())
                        .isEqualTo(409));

        verify(attributeMetaCache, never()).evictCurrentTenant();
    }

    @Test
    void deletingIdentitySourceRecomposesAllIdentities() {
        Attribute current = attribute(42L, null);
        current.setIdentitySource(true);
        when(attributeDao.findById(42L)).thenReturn(Optional.of(current));

        service.delete(42L);

        verify(identityService).synchronizeAllCurrentTenant(any());
    }

    @Test
    void rejectsDeletionOfIdentitySystemAttribute() {
        Attribute current = attribute(42L, 9L);
        current.setConceptCode("IDENTITY");
        when(attributeDao.findById(42L)).thenReturn(Optional.of(current));

        assertThatThrownBy(() -> service.delete(42L))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode().value())
                        .isEqualTo(403));

        verify(attributeDao, never()).delete(42L);
    }

    @Test
    void reorderingIdentitySourceAttributesNeverRecomposesIdentity() {
        // MVP: displayOrder is purely administrable presentation. Composition order
        // is semantic (FIRST_NAME then LAST_NAME) and reordering must never trigger
        // a resynchronization, even when identity-source attributes are involved.
        TenantContext.set(10L);
        Attribute first = attribute(11L, null);
        first.setIdentitySource(true);
        first.setDisplayOrder(10);
        Attribute second = attribute(12L, null);
        second.setDisplayOrder(20);
        when(attributeDao.findAllByIdsForTenant(eq(10L), anyList()))
                .thenReturn(List.of(first, second));

        service.reorder(List.of(
                new AttributeService.OrderUpdate(11L, 20),
                new AttributeService.OrderUpdate(12L, 10)));

        assertThat(first.getDisplayOrder()).isEqualTo(20);
        assertThat(second.getDisplayOrder()).isEqualTo(10);
        verify(attributeDao).saveAll(List.of(first, second));
        verify(attributeMetaCache).evictCurrentTenant();
        verify(identityService, never()).synchronizeAllCurrentTenant(any());
    }

    @Test
    void rejectsDuplicateIdsInReorderPayload() {
        TenantContext.set(10L);

        assertThatThrownBy(() -> service.reorder(List.of(
                new AttributeService.OrderUpdate(11L, 10),
                new AttributeService.OrderUpdate(11L, 20))))
                .isInstanceOf(com.saymyname.core.exception.common.ValidationException.class);

        verify(attributeDao, never()).saveAll(anyList());
    }

    private void stubConcept(Long conceptId) {
        Concept concept = new Concept.Builder()
                .withId(conceptId)
                .withValueType(ValueType.TEXT)
                .build();
        when(conceptDao.findById(conceptId)).thenReturn(Optional.of(concept));
    }

    private void stubGenderConcept(Long conceptId) {
        Concept concept = new Concept.Builder()
                .withId(conceptId)
                .withCode(ConceptCodes.GENDER)
                .withValueType(ValueType.ENUM)
                .withIdentityComponentEligible(false)
                .withRequiredMaxValues(1)
                .build();
        when(conceptDao.findById(conceptId)).thenReturn(Optional.of(concept));
    }

    private void stubConcept(Long conceptId, String code, boolean identityComponentEligible) {
        Concept concept = new Concept.Builder()
                .withId(conceptId)
                .withCode(code)
                .withValueType(ValueType.TEXT)
                .withIdentityComponentEligible(identityComponentEligible)
                .build();
        when(conceptDao.findById(conceptId)).thenReturn(Optional.of(concept));
    }

    private Attribute attribute(Long id, Long conceptId) {
        return new Attribute.Builder()
                .withId(id)
                .withConceptId(conceptId)
                .withName("Field" + (id == null ? "" : " " + id))
                .withType(ValueType.TEXT)
                .withMaxValues(1)
                .build();
    }
}
