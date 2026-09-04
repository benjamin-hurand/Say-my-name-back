package com.saymyname.service.identity;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.InOrder;
import org.mockito.junit.jupiter.MockitoExtension;

import com.saymyname.core.identity.IdentityResolver;
import com.saymyname.core.model.people.Fact;
import com.saymyname.persistence.dao.FactDao;
import com.saymyname.persistence.dao.PersonDao;
import com.saymyname.service.attribute.AttributeMetaCache;

@ExtendWith(MockitoExtension.class)
class IdentityServiceTest {

    private static final Long PERSON_ID = 7L;
    private static final Long FIRST_NAME_ID = 11L;
    private static final Long LAST_NAME_ID = 12L;
    private static final Long IDENTITY_ID = 13L;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 25, 12, 0);

    @Mock
    private AttributeMetaCache attributeMetaCache;
    @Mock
    private FactDao factDao;
    @Mock
    private PersonDao personDao;

    private IdentityService service;

    @BeforeEach
    void setUp() {
        service = new IdentityService(attributeMetaCache, factDao, new IdentityResolver(), personDao);
        when(attributeMetaCache.getIdentityAttributeId()).thenReturn(IDENTITY_ID);
        when(attributeMetaCache.getIdentitySourceAttributeIds()).thenReturn(List.of(FIRST_NAME_ID, LAST_NAME_ID));
    }

    @Test
    void createsIdentityFromOrderedSources() {
        source(FIRST_NAME_ID, fact(1L, "Jean"));
        source(LAST_NAME_ID, fact(2L, "DUPONT"));
        source(IDENTITY_ID);

        service.synchronize(PERSON_ID, NOW);

        verify(factDao).createAllForPersonAt(PERSON_ID, IDENTITY_ID, List.of("Jean DUPONT"), NOW);
        InOrder calls = inOrder(factDao);
        calls.verify(factDao).lockPersonForUpdate(PERSON_ID);
        calls.verify(factDao).findActiveAtByPersonAndAttribute(PERSON_ID, FIRST_NAME_ID, NOW);
    }

    @Test
    void closesPreviousIdentityAndCreatesReplacement() {
        source(FIRST_NAME_ID, fact(1L, "John"));
        source(LAST_NAME_ID, fact(2L, "DUPONT"));
        source(IDENTITY_ID, fact(30L, "Jean DUPONT"));

        service.synchronize(PERSON_ID, NOW);

        verify(factDao).softCloseActiveByIdsAndPersonId(PERSON_ID, List.of(30L), NOW);
        verify(factDao).createAllForPersonAt(PERSON_ID, IDENTITY_ID, List.of("John DUPONT"), NOW);
    }

    @Test
    void replacesIdentityWhenLastNameChanges() {
        source(FIRST_NAME_ID, fact(1L, "Jean"));
        source(LAST_NAME_ID, fact(2L, "MARTIN"));
        source(IDENTITY_ID, fact(30L, "Jean DUPONT"));

        service.synchronize(PERSON_ID, NOW);

        verify(factDao).softCloseActiveByIdsAndPersonId(PERSON_ID, List.of(30L), NOW);
        verify(factDao).createAllForPersonAt(PERSON_ID, IDENTITY_ID, List.of("Jean MARTIN"), NOW);
    }

    @Test
    void recomposesAfterOneSourceIsRemoved() {
        source(FIRST_NAME_ID, fact(1L, "Jean"));
        source(LAST_NAME_ID);
        source(IDENTITY_ID, fact(30L, "Jean DUPONT"));

        service.synchronize(PERSON_ID, NOW);

        verify(factDao).createAllForPersonAt(PERSON_ID, IDENTITY_ID, List.of("Jean"), NOW);
    }

    @Test
    void composesIdentityFromLastNameAlone() {
        source(FIRST_NAME_ID);
        source(LAST_NAME_ID, fact(2L, "DUPONT"));
        source(IDENTITY_ID);

        service.synchronize(PERSON_ID, NOW);

        verify(factDao).createAllForPersonAt(PERSON_ID, IDENTITY_ID, List.of("DUPONT"), NOW);
    }

    @Test
    void closesIdentityWithoutCreatingBlankFactWhenAllSourcesAreRemoved() {
        source(FIRST_NAME_ID);
        source(LAST_NAME_ID);
        source(IDENTITY_ID, fact(30L, "Jean"));

        service.synchronize(PERSON_ID, NOW);

        verify(factDao).softCloseActiveByIdsAndPersonId(PERSON_ID, List.of(30L), NOW);
        verify(factDao, never()).createAllForPersonAt(eq(PERSON_ID), eq(IDENTITY_ID), anyList(), eq(NOW));
    }

    @Test
    void synchronizesEveryPersonInCurrentTenant() {
        when(personDao.findAllIds()).thenReturn(List.of(7L, 8L));
        sourceForPerson(7L, FIRST_NAME_ID, fact(1L, "Jean"));
        sourceForPerson(7L, LAST_NAME_ID);
        sourceForPerson(7L, IDENTITY_ID);
        sourceForPerson(8L, FIRST_NAME_ID, fact(2L, "Alice"));
        sourceForPerson(8L, LAST_NAME_ID);
        sourceForPerson(8L, IDENTITY_ID);

        service.synchronizeAllCurrentTenant(NOW);

        verify(factDao).createAllForPersonAt(7L, IDENTITY_ID, List.of("Jean"), NOW);
        verify(factDao).createAllForPersonAt(8L, IDENTITY_ID, List.of("Alice"), NOW);
    }

    private void source(Long attributeId, Fact... facts) {
        when(factDao.findActiveAtByPersonAndAttribute(PERSON_ID, attributeId, NOW))
                .thenReturn(List.of(facts));
    }

    private void sourceForPerson(Long personId, Long attributeId, Fact... facts) {
        when(factDao.findActiveAtByPersonAndAttribute(personId, attributeId, NOW))
                .thenReturn(List.of(facts));
    }

    private Fact fact(Long id, String value) {
        return new Fact.Builder().withId(id).withValue(value).build();
    }
}
