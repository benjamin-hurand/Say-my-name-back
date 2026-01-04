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
import com.saymyname.core.model.game.options.GameMode;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.core.model.persondirectory.AdminPersonCard;
import com.saymyname.core.model.persondirectory.AdminPersonSearchCriteria;
import com.saymyname.core.model.persondirectory.AttributeValueRow;
import com.saymyname.core.model.persondirectory.AttributeValueView;
import com.saymyname.core.model.persondirectory.PagePersonRow;
import com.saymyname.core.model.persondirectory.PersonCard;
import com.saymyname.core.model.persondirectory.PersonSearchCriteria;
import com.saymyname.persistence.dao.PersonDao;
import com.saymyname.service.PersonAttributeService;
import com.saymyname.service.UserOrganizationService;

@Service
@Transactional
public class PersonService {

    private final PersonDao personDao;
    private final PersonAttributeService personAttributeService;
    private final UserOrganizationService userOrganizationService;

    public PersonService(PersonDao personDao,
            PersonAttributeService personAttributeService, UserOrganizationService userOrganizationService) {
        this.personDao = personDao;
        this.personAttributeService = personAttributeService;
        this.userOrganizationService = userOrganizationService;
    }

    public List<Person> findAll() {
        return personDao.findAll();
    }

    public long countAll() {
        return personDao.countAll();
    }

    public long countUniverseEligibleForMode(Course course) {
        GameMode gameMode = course.getGameMode();
        Long gameModeId = gameMode.getId();
        String op = gameMode.getOperator();
        String operator = (op == null || op.isBlank()) ? "AND" : op.trim();
        if ("AND".equalsIgnoreCase(operator)) {
            return personDao.countUniverseEligibleAND(gameModeId);
        } else {
            return personDao.countUniverseEligibleOR(gameModeId);
        }
    }

    public Optional<Person> findById(Long id) {
        return personDao.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Person> getPersonByIdWithAllAttributes(Long personId) {
        if (personId == null)
            return Optional.empty();
        return personDao.loadWithAttributesAndPhotos(personId);
    }

    @Transactional(readOnly = true)
    public Optional<Person> getPersonByUserWithAllAttributes(User user) {
        if (user == null || user.getId() == null)
            return Optional.empty();

        return userOrganizationService.findPersonIdByUserId(user.getId())
                .flatMap(personDao::loadWithAttributesAndPhotos);
    }

    /**
     * Recherche “trombi” côté utilisateur (avec notion de suivi).
     * - Page de personnes (id + photo)
     * - Attributs primaires + (optionnel) extras “contexte”
     * - Marquage “followed” sur le batch
     * - ✅ Désormais on remplit une liste unique `attributes` (avec primaryField).
     */
    @Transactional(readOnly = true)
    public Page<PersonCard> searchPersons(PersonSearchCriteria criteria, Pageable pageable, Long userId) {
        // 1) Page minimale (id + photo)
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

        // 2) IDs suivis pour ce batch (si userId fourni)
        final Set<Long> followedIds = (userId == null)
                ? Set.of()
                : personDao.findFollowedIdsForUserAndPersons(userId, personIds);

        // 3) Primaires (marqués primaryField=true)
        final Map<Long, List<AttributeValueView>> primaryByPerson = toViewListByPersonWithPrimary(
                personDao.fetchPrimaryAttributeRows(personIds), true);

        // 4) Extras "contexte" si demandé (marqués primaryField=false)
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
                    /* includeCategories */ true,
                    /* includeFilterSortAttributes */ true);

            extrasByPerson = toViewListByPersonWithPrimary(ctxRows, false);
        } else {
            extrasByPerson = Map.of();
        }

        // 5) Fusion en une liste unique (primaires + extras)
        final Map<Long, List<AttributeValueView>> allAttributesByPerson = new HashMap<>();
        for (Long id : personIds) {
            List<AttributeValueView> merged = new ArrayList<>();
            merged.addAll(primaryByPerson.getOrDefault(id, List.of()));
            merged.addAll(extrasByPerson.getOrDefault(id, List.of()));
            allAttributesByPerson.put(id, merged);
        }

        // 6) Assemblage final
        return page.map(p -> new PersonCard.Builder()
                .withIdPerson(p.getPersonId())
                .withPhotoStorageKey(p.getPhotoStorageKey())
                .withAttributes(allAttributesByPerson.getOrDefault(p.getPersonId(), List.of()))
                .withFollowed(followedIds.contains(p.getPersonId()))
                .build());
    }

    /**
     * Recherche “trombi” côté admin (pas de notion de suivi).
     * - Page de personnes (id + photo)
     * - Attributs primaires + (optionnel) extras “contexte”
     * - (Optionnel) hasPendingChangeRequests
     * - ✅ Désormais on remplit une liste unique `attributes` (avec primaryField).
     */
    @Transactional(readOnly = true)
    public Page<AdminPersonCard> searchPersonsForAdmin(AdminPersonSearchCriteria criteria, Pageable pageable) {
        // 1) Page minimale (id + photo)
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

        // 2) Primaires (primaryField=true)
        final Map<Long, List<AttributeValueView>> primaryByPerson = toViewListByPersonWithPrimary(
                personDao.fetchPrimaryAttributeRows(personIds), true);

        // 3) Extras "contexte" si demandé (primaryField=false)
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
                    /* includeCategories */ true,
                    /* includeFilterSortAttributes */ true);

            extrasByPerson = toViewListByPersonWithPrimary(ctxRows, false);
        } else {
            extrasByPerson = Map.of();
        }

        // 4) (Facultatif) personnes avec CR en attente (placeholder)
        final Set<Long> pendingCR = Set.of();

        // 5) Fusion en une liste unique (primaires + extras)
        final Map<Long, List<AttributeValueView>> allAttributesByPerson = new HashMap<>();
        for (Long id : personIds) {
            List<AttributeValueView> merged = new ArrayList<>();
            merged.addAll(primaryByPerson.getOrDefault(id, List.of()));
            merged.addAll(extrasByPerson.getOrDefault(id, List.of()));
            allAttributesByPerson.put(id, merged);
        }

        // 6) Assemblage final
        return page.map(p -> new AdminPersonCard.Builder()
                .withIdPerson(p.getPersonId())
                .withPhotoStorageKey(p.getPhotoStorageKey())
                .withAttributes(allAttributesByPerson.getOrDefault(p.getPersonId(), List.of()))
                .withHasPendingChangeRequests(pendingCR.contains(p.getPersonId()))
                .build());
    }

    public List<Long> findPersonIds(PersonSearchCriteria criteria, Long userId) {
        return personDao.findPersonIds(criteria, userId);
    }

    public List<PersonAttribute> applyAttributeChangesForUser(
            User user,
            Long attributeId,
            List<PersonAttribute> toCreate,
            List<PersonAttribute> toUpdate,
            List<PersonAttribute> toDelete) {

        if (user == null || user.getId() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié");
        }

        Long personId = userOrganizationService.findPersonIdByUserId(user.getId())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Profil introuvable"));

        return personAttributeService.applyChangesForPerson(
                personId, attributeId, toCreate, toUpdate, toDelete, false);
    }

    // -----------------------
    // Helpers internes
    // -----------------------

    /**
     * Regroupe des rows (personId, attributeId, value, displayOrder) par personne
     * et les transforme en AttributeValueView, en posant le flag primaryField.
     */
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
                                .withPrimaryField(primaryFlag) // ⬅️ important
                                .build(),
                        Collectors.toList())));
    }
}
