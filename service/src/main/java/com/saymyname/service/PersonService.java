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
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.PersonAttribute;
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

    public Optional<Person> findById(Long id) {
        return personDao.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Person> getPersonByUserWithAllAttributes(User user) {
        if (user == null || user.getId() == null)
            return Optional.empty();

        Optional<Long> personIdOpt = personDao.findPersonIdByUserId(user.getId());
        if (personIdOpt.isEmpty())
            return Optional.empty();
        Long personId = personIdOpt.get();

        personDao.preloadAttributesGraph(personId);
        personDao.preloadPhotos(personId);

        return personDao.mapManagedToModel(personId);
    }

    @Transactional(readOnly = true)
    public Page<PersonCard> searchPersons(PersonSearchCriteria criteria, Pageable pageable, Long userId) {
        // 1) Page des personnes (id + photo storageKey)
        Page<PersonDao.PagePerson> page = personDao.findPersonsPage(criteria, pageable, userId);

        List<Long> personIds = page.getContent().stream()
                .map(PersonDao.PagePerson::personId)
                .toList();

        if (personIds.isEmpty()) {
            return page.map(p -> new PersonCard.Builder()
                    .withIdPerson(p.personId())
                    .withPhotoStorageKey(p.photoStorageKey())
                    .withPrimaryAttributes(List.of())
                    .withFollowed(false)
                    .withExtraAttributes(List.of())
                    .build());
        }

        // 2) IDs suivis sur ce batch
        final Set<Long> followedIds = personDao.findFollowedIdsForUserAndPersons(userId, personIds);

        // 3) Attributs primaires sur ce batch
        final Map<Long, List<PersonCard.PrimaryAttribute>> attrsByPerson = personDao.fetchPrimaryAttributes(personIds)
                .stream()
                .collect(Collectors.groupingBy(
                        PersonDao.PrimaryAttrRow::personId,
                        Collectors.mapping(r -> new PersonCard.PrimaryAttribute.Builder()
                                .withPersonAttributeId(r.personAttributeId())
                                .withAttributeId(r.attributeId())
                                .withValue(r.value())
                                .withDisplayOrder(r.displayOrder())
                                .withPrimary(true)
                                .build(),
                                Collectors.toList())));

        // 4) (NOUVEAU) Attributs “contexte” si demandé
        Map<Long, List<PersonCard.PersonAttributeExtra>> tmpExtrasByPerson;
        if (criteria != null && criteria.isIncludeContextAttributes()) {
            // IDs d’attributs utilisés dans les filtres (⚠️ exclure -1 et ids <=0)
            List<Long> filterIds = (criteria.getFilters() == null) ? List.of()
                    : criteria.getFilters().stream()
                            .map(f -> f.getAttributeId())
                            .filter(Objects::nonNull)
                            .filter(id -> id > 0) // ⬅️ ignore l'id global de recherche (-1) et tout <=0
                            .distinct()
                            .toList();

            // IDs d’attributs utilisés dans les tris (kind=ATTRIBUTE)
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

            var rows = personDao.fetchContextAttributes(personIds, contextAttrIds, true, true);

            tmpExtrasByPerson = rows.stream().collect(Collectors.groupingBy(
                    PersonDao.AnyAttrRow::personId,
                    Collectors.mapping(r -> new PersonCard.PersonAttributeExtra.Builder()
                            .withAttributeId(r.attributeId())
                            .withValue(r.value())
                            .withDisplayOrder(r.displayOrder())
                            .build(),
                            Collectors.toList())));
        } else {
            tmpExtrasByPerson = Map.of();
        }

        // IMPORTANT : variable capturée par la lambda, donc final/effectively final
        final Map<Long, List<PersonCard.PersonAttributeExtra>> finalExtrasByPerson = tmpExtrasByPerson;

        // 5) Assemblage
        return page.map(p -> new PersonCard.Builder()
                .withIdPerson(p.personId())
                .withPhotoStorageKey(p.photoStorageKey())
                .withPrimaryAttributes(attrsByPerson.getOrDefault(p.personId(), List.of()))
                .withFollowed(followedIds.contains(p.personId()))
                .withExtraAttributes(finalExtrasByPerson.getOrDefault(p.personId(), List.of()))
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

        return personAttributeService.applyChangesForPerson(personId, attributeId, toCreate, toUpdate, toDelete);
    }
}
