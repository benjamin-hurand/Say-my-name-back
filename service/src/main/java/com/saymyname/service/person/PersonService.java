// src/main/java/com/saymyname/service/PersonService.java
package com.saymyname.service.person;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.Fact;
import com.saymyname.core.model.persondirectory.AdminPersonCard;
import com.saymyname.core.model.persondirectory.AdminPersonSearchCriteria;
import com.saymyname.core.model.persondirectory.AttributeValueRow;
import com.saymyname.core.model.persondirectory.AttributeValueView;
import com.saymyname.core.model.persondirectory.PagePersonRow;
import com.saymyname.core.model.persondirectory.PersonCard;
import com.saymyname.core.model.persondirectory.PersonSearchCriteria;
import com.saymyname.persistence.dao.PersonDao;
import com.saymyname.service.FactService;
import com.saymyname.service.attribute.AttributeMetaCache;
import com.saymyname.service.tenant.TenantMembershipService;

@Service
@Transactional
public class PersonService {

    private final PersonDao personDao;
    private final FactService factService;
    private final TenantMembershipService tenantMembershipService;
    private final AttributeMetaCache attributeMetaCache;

    public PersonService(
            PersonDao personDao,
            FactService factService,
            TenantMembershipService tenantMembershipService,
            AttributeMetaCache attributeMetaCache) {
        this.personDao = personDao;
        this.factService = factService;
        this.tenantMembershipService = tenantMembershipService;
        this.attributeMetaCache = attributeMetaCache;
    }

    public List<Person> findAll() {
        return personDao.findAll();
    }

    public long countAll() {
        return personDao.countAll();
    }

    /**
     * ✅ Universe eligibility pour un Course.
     * Nouveau modèle: Course cible toujours UN SEUL attribut (normal ou derived).
     */
    public long countUniverseEligibleForMode(Course course) {
        if (course == null) {
            return 0L;
        }
        Long targetAttributeId = course.getTargetAttributeId();
        if (targetAttributeId == null) {
            // En théorie impossible si tu passes courses.target_attribute_id NOT NULL,
            // mais garde la guardrail.
            return 0L;
        }

        // Éligible = la personne possède une fact active (non vide) pour cet attribut.
        return personDao.countUniverseEligibleOneAttribute(targetAttributeId);
    }

    public Optional<Person> findById(Long id) {
        return personDao.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Person> getPersonByIdWithAllAttributes(Long personId) {
        if (personId == null)
            return Optional.empty();
        return personDao.loadWithFactsAndPhotos(personId);
    }

    @Transactional(readOnly = true)
    public Optional<Person> getPersonByUserWithAllAttributes(User user) {
        if (user == null || user.getId() == null)
            return Optional.empty();

        return tenantMembershipService.findPersonIdByUserId(user.getId())
                .flatMap(personDao::loadWithFactsAndPhotos);
    }

    /**
     * Recherche “trombi” côté utilisateur (avec notion de suivi).
     */
    @Transactional(readOnly = true)
    public Page<PersonCard> searchPersons(PersonSearchCriteria criteria, Pageable pageable, Long userId) {
        Page<PagePersonRow> page = personDao.findPersonsPage(criteria, pageable, userId);

        List<Long> personIds = page.getContent().stream()
                .map(PagePersonRow::getPersonId)
                .toList();

        if (personIds.isEmpty()) {
            return page.map(p -> new PersonCard.Builder()
                    .withIdPerson(p.getPersonId())
                    .withPhotoStorageKey(p.getPhotoStorageKey())
                    .withAttributes(List.of())
                    .withFollowed(false)
                    .build());
        }

        final Set<Long> followedIds = (userId == null)
                ? Set.of()
                : personDao.findFollowedIdsForUserAndPersons(userId, personIds);

        final Map<Long, List<AttributeValueView>> primaryByPerson = toViewListByPersonWithPrimary(
                personDao.fetchPrimaryAttributeRows(personIds), true);
        final Map<Long, String> displayNamesByPerson = fetchDisplayNames(personIds);

        final Map<Long, List<AttributeValueView>> extrasByPerson;
        if (criteria != null && criteria.isIncludeContextAttributes()) {
            List<Long> filterIds = (criteria.getFilters() == null) ? List.of()
                    : criteria.getFilters().stream()
                            .map(f -> f.getAttributeId())
                            .filter(Objects::nonNull)
                            .filter(id -> id > 0)
                            .distinct()
                            .toList();

            List<Long> sortAttrIds = (criteria.getSort() == null) ? List.of()
                    : criteria.getSort().stream()
                            .filter(s -> "ATTRIBUTE".equalsIgnoreCase(s.getKind()))
                            .map(s -> s.getAttributeId())
                            .filter(Objects::nonNull)
                            .filter(id -> id > 0)
                            .distinct()
                            .toList();

            List<Long> contextAttrIds = Stream.concat(filterIds.stream(), sortAttrIds.stream())
                    .distinct()
                    .toList();

            List<AttributeValueRow> ctxRows = personDao.fetchContextAttributes(
                    personIds,
                    contextAttrIds,
                    /* includeFilterSortAttributes */ true);

            extrasByPerson = toViewListByPersonWithPrimary(ctxRows, false);
        } else {
            extrasByPerson = Map.of();
        }

        final Map<Long, List<AttributeValueView>> allAttributesByPerson = new HashMap<>();
        for (Long id : personIds) {
            List<AttributeValueView> merged = new ArrayList<>();
            merged.addAll(primaryByPerson.getOrDefault(id, List.of()));
            merged.addAll(extrasByPerson.getOrDefault(id, List.of()));
            allAttributesByPerson.put(id, merged);
        }

        return page.map(p -> new PersonCard.Builder()
                .withIdPerson(p.getPersonId())
                .withPhotoStorageKey(p.getPhotoStorageKey())
                .withDisplayName(displayNamesByPerson.getOrDefault(p.getPersonId(), ""))
                .withAttributes(allAttributesByPerson.getOrDefault(p.getPersonId(), List.of()))
                .withFollowed(followedIds.contains(p.getPersonId()))
                .build());
    }

    /**
     * Recherche “trombi” côté admin (pas de notion de suivi).
     */
    @Transactional(readOnly = true)
    public Page<AdminPersonCard> searchPersonsForAdmin(AdminPersonSearchCriteria criteria, Pageable pageable) {
        Page<PagePersonRow> page = personDao.findPersonsPageForAdmin(criteria, pageable);

        List<Long> personIds = page.getContent().stream()
                .map(PagePersonRow::getPersonId)
                .toList();

        if (personIds.isEmpty()) {
            return page.map(p -> new AdminPersonCard.Builder()
                    .withIdPerson(p.getPersonId())
                    .withPhotoStorageKey(p.getPhotoStorageKey())
                    .withAttributes(List.of())
                    .withHasPendingChangeRequests(false)
                    .build());
        }

        final Map<Long, List<AttributeValueView>> primaryByPerson = toViewListByPersonWithPrimary(
                personDao.fetchPrimaryAttributeRows(personIds), true);
        final Map<Long, String> displayNamesByPerson = fetchDisplayNames(personIds);

        final Map<Long, List<AttributeValueView>> extrasByPerson;
        if (criteria != null && criteria.isIncludeContextAttributes()) {
            List<Long> filterIds = (criteria.getFilters() == null) ? List.of()
                    : criteria.getFilters().stream()
                            .map(f -> f.getAttributeId())
                            .filter(Objects::nonNull)
                            .filter(id -> id > 0)
                            .distinct()
                            .toList();

            List<Long> sortAttrIds = (criteria.getSort() == null) ? List.of()
                    : criteria.getSort().stream()
                            .filter(s -> "ATTRIBUTE".equalsIgnoreCase(s.getKind()))
                            .map(s -> s.getAttributeId())
                            .filter(Objects::nonNull)
                            .filter(id -> id > 0)
                            .distinct()
                            .toList();

            List<Long> contextAttrIds = Stream.concat(filterIds.stream(), sortAttrIds.stream())
                    .distinct()
                    .toList();

            List<AttributeValueRow> ctxRows = personDao.fetchContextAttributes(
                    personIds,
                    contextAttrIds,
                    /* includeFilterSortAttributes */ true);

            extrasByPerson = toViewListByPersonWithPrimary(ctxRows, false);
        } else {
            extrasByPerson = Map.of();
        }

        final Set<Long> pendingCR = Set.of();

        final Map<Long, List<AttributeValueView>> allAttributesByPerson = new HashMap<>();
        for (Long id : personIds) {
            List<AttributeValueView> merged = new ArrayList<>();
            merged.addAll(primaryByPerson.getOrDefault(id, List.of()));
            merged.addAll(extrasByPerson.getOrDefault(id, List.of()));
            allAttributesByPerson.put(id, merged);
        }

        return page.map(p -> new AdminPersonCard.Builder()
                .withIdPerson(p.getPersonId())
                .withPhotoStorageKey(p.getPhotoStorageKey())
                .withDisplayName(displayNamesByPerson.getOrDefault(p.getPersonId(), ""))
                .withAttributes(allAttributesByPerson.getOrDefault(p.getPersonId(), List.of()))
                .withHasPendingChangeRequests(pendingCR.contains(p.getPersonId()))
                .build());
    }

    public List<Long> findPersonIds(PersonSearchCriteria criteria, Long userId) {
        return personDao.findPersonIds(criteria, userId);
    }

    public List<Fact> applyAttributeChangesForUser(
            User user,
            Long attributeId,
            List<Fact> toCreate,
            List<Fact> toUpdate,
            List<Fact> toDelete) {

        if (user == null || user.getId() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié");
        }

        Long personId = tenantMembershipService.findPersonIdByUserId(user.getId())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Profil introuvable"));

        return factService.applyChangesForPerson(
                personId, attributeId, toCreate, toUpdate, toDelete, false);
    }

    // -----------------------
    // Helpers internes
    // -----------------------

    private static Map<Long, List<AttributeValueView>> toViewListByPersonWithPrimary(
            List<AttributeValueRow> rows,
            boolean primaryFlag) {

        if (rows == null || rows.isEmpty())
            return Map.of();

        return rows.stream().collect(Collectors.groupingBy(
                AttributeValueRow::getPersonId,
                Collectors.mapping(
                        r -> new AttributeValueView.Builder()
                                .withAttributeId(r.getAttributeId())
                                .withValue(r.getValue())
                                .withDisplayOrder(r.getDisplayOrder())
                                .withIdentitySource(primaryFlag)
                                .build(),
                        Collectors.toList())));
    }

    private Map<Long, String> fetchDisplayNames(List<Long> personIds) {
        Long identityAttributeId = attributeMetaCache.getIdentityAttributeId();
        if (identityAttributeId == null) {
            return Map.of();
        }

        return personDao.fetchContextAttributes(personIds, List.of(identityAttributeId), false).stream()
                .filter(row -> row.getValue() != null && !row.getValue().isBlank())
                .collect(Collectors.toMap(
                        AttributeValueRow::getPersonId,
                        row -> row.getValue().trim(),
                        (first, ignored) -> first));
    }
}
