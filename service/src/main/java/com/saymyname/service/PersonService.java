// src/main/java/com/saymyname/service/PersonService.java
package com.saymyname.service;

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

@Service
@Transactional
public class PersonService {

    private final PersonDao personDao;
    private final PersonAttributeService personAttributeService;

    public PersonService(PersonDao personDao,
            PersonAttributeService personAttributeService) {
        this.personDao = personDao;
        this.personAttributeService = personAttributeService;
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

        return personDao.findPersonIdByUserId(user.getId())
                .flatMap(personDao::loadWithAttributesAndPhotos);
    }

    /**
     * Recherche “trombi” côté utilisateur (avec notion de suivi).
     * - Page de personnes (id + photo)
     * - Attributs primaires groupés par personne
     * - (optionnel) Attributs “contexte” (filtres/tri/catégories)
     * - Marquage “followed” sur le batch
     */
    @Transactional(readOnly = true)
    public Page<PersonCard> searchPersons(PersonSearchCriteria criteria, Pageable pageable, Long userId) {
        // 1) Page de lignes minimales (id + photo)
        Page<PagePersonRow> page = personDao.findPersonsPage(criteria, pageable, userId);

        List<Long> personIds = page.getContent().stream()
                .map(PagePersonRow::getPersonId)
                .toList();

        if (personIds.isEmpty()) {
            return page.map(p -> new PersonCard.Builder()
                    .withIdPerson(p.getPersonId())
                    .withPhotoStorageKey(p.getPhotoStorageKey())
                    .withPrimaryAttributes(List.of())
                    .withFollowed(false)
                    .withExtraAttributes(List.of())
                    .build());
        }

        // 2) IDs suivis sur ce batch (si userId fourni)
        final Set<Long> followedIds = (userId == null)
                ? Set.of()
                : personDao.findFollowedIdsForUserAndPersons(userId, personIds);

        // 3) Attributs primaires de ce batch
        // ⚠️ On suppose que tu exposes une méthode DAO qui renvoie des rows uniformes:
        // List<AttributeValueRow> fetchPrimaryAttributeRows(List<Long> personIds)
        // Si tu ne l’as pas encore, ajoute-la côté PersonDao (projection = personId,
        // attributeId, value, displayOrder).
        final Map<Long, List<AttributeValueView>> primaryByPerson = toViewListByPerson(
                personDao.fetchPrimaryAttributeRows(personIds));

        // 4) Attributs “contexte” si demandé (catégories + attributs utilisés pour
        // filtrer/ trier)
        final Map<Long, List<AttributeValueView>> extrasByPerson;
        if (criteria != null && criteria.isIncludeContextAttributes()) {
            // IDs d’attributs présents dans les filtres (exclure l’id spécial -1 et <=0)
            List<Long> filterIds = (criteria.getFilters() == null) ? List.of()
                    : criteria.getFilters().stream()
                            .map(f -> f.getAttributeId())
                            .filter(Objects::nonNull)
                            .filter(id -> id > 0)
                            .distinct()
                            .toList();

            // IDs d’attributs présents dans le tri (kind=ATTRIBUTE)
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

            // includeCategories=true, includeFilterSortAttributes=true
            List<AttributeValueRow> ctxRows = personDao.fetchContextAttributes(
                    personIds, contextAttrIds, true, true);

            extrasByPerson = toViewListByPerson(ctxRows);
        } else {
            extrasByPerson = Map.of();
        }

        // 5) Assemblage final
        return page.map(p -> new PersonCard.Builder()
                .withIdPerson(p.getPersonId())
                .withPhotoStorageKey(p.getPhotoStorageKey())
                .withPrimaryAttributes(primaryByPerson.getOrDefault(p.getPersonId(), List.of()))
                .withFollowed(followedIds.contains(p.getPersonId()))
                .withExtraAttributes(extrasByPerson.getOrDefault(p.getPersonId(), List.of()))
                .build());
    }

    /**
     * Recherche “trombi” côté admin (pas de notion de suivi).
     * - Page de personnes (id + photo)
     * - Attributs primaires
     * - (Optionnel tant que non implémenté) hasPendingChangeRequests
     */
    @Transactional(readOnly = true)
    public Page<AdminPersonCard> searchPersonsForAdmin(AdminPersonSearchCriteria criteria, Pageable pageable) {
        Page<PagePersonRow> page = personDao.findPersonsPageForAdmin(criteria, pageable);

        List<Long> personIds = page.getContent().stream()
                .map(PagePersonRow::getPersonId)
                .toList();

        final Map<Long, List<AttributeValueView>> primaryByPerson = toViewListByPerson(
                personDao.fetchPrimaryAttributeRows(personIds));

        // Hook (facultatif) : si tu ajoutes un DAO/Repo qui renvoie les IDs des
        // personnes
        // avec CR en statut PENDING, mappe-les ici :
        // final Set<Long> pendingCR =
        // changeRequestDao.findPersonIdsWithPendingRequests(personIds);
        final Set<Long> pendingCR = Set.of(); // par défaut: false partout

        return page.map(p -> new AdminPersonCard.Builder()
                .withIdPerson(p.getPersonId())
                .withPhotoStorageKey(p.getPhotoStorageKey())
                .withPrimaryAttributes(primaryByPerson.getOrDefault(p.getPersonId(), List.of()))
                .withExtraAttributes(List.of()) // tu peux aussi injecter un contexte si tu veux
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

        Long personId = personDao.findPersonIdByUserId(user.getId())
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
     * et les transforme en AttributeValueView (sans personId, sans PA id).
     */
    private static Map<Long, List<AttributeValueView>> toViewListByPerson(List<AttributeValueRow> rows) {
        if (rows == null || rows.isEmpty())
            return Map.of();

        return rows.stream().collect(Collectors.groupingBy(
                AttributeValueRow::getPersonId,
                Collectors.mapping(
                        r -> new AttributeValueView.Builder()
                                .withAttributeId(r.getAttributeId())
                                .withValue(r.getValue())
                                .withDisplayOrder(r.getDisplayOrder())
                                .build(),
                        Collectors.toList())));
    }
}
