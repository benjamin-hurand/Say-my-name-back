package com.saymyname.persistence.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.exception.profile.RequiredAttributeDeletionException;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.persistence.entity.AttributeEntity;
import com.saymyname.persistence.entity.PersonAttributeEntity;
import com.saymyname.persistence.entity.PersonEntity;
import com.saymyname.persistence.mapper.PersonAttributeEntityMapper;
import com.saymyname.persistence.repository.AttributeRepository;
import com.saymyname.persistence.repository.PersonAttributeRepository;
import com.saymyname.persistence.repository.PersonRepository;

@Repository
@Transactional
public class PersonAttributeDao {

    private final PersonAttributeRepository personAttributeRepository;
    private final PersonAttributeEntityMapper personAttributeEntityMapper;
    private final AttributeRepository attributeRepository;
    private final PersonRepository personRepository;

    public PersonAttributeDao(PersonAttributeRepository personAttributeRepository,
            PersonAttributeEntityMapper personAttributeEntityMapper,
            AttributeRepository attributeRepository,
            PersonRepository personRepository) {
        this.personAttributeRepository = personAttributeRepository;
        this.personAttributeEntityMapper = personAttributeEntityMapper;
        this.attributeRepository = attributeRepository;
        this.personRepository = personRepository;
    }

    public List<PersonAttribute> findAttributesByPhotoId(Long photoId) {
        return personAttributeRepository
                .findAttributesByPhotoId(photoId)
                .stream()
                .map(personAttributeEntityMapper::toFullModel)
                .toList();
    }

    public Long countPersonsMatchingFilter(String minValue, String nextValue, LocalDateTime validFor,
            Long attributeId) {
        return personAttributeRepository.countPersonsMatchingFilter(minValue, nextValue, validFor, attributeId);
    }

    public List<PersonAttribute> findAttributesByPersonId(Long personId) {
        return personAttributeRepository
                .findAttributesByPersonId(personId)
                .stream()
                .map(personAttributeEntityMapper::toFullModel)
                .toList();
    }

    public boolean attributeExists(Long attributeId) {
        return attributeRepository.existsById(attributeId);
    }

    public boolean isAttributeUnique(Long attributeId) {
        return attributeRepository.findById(attributeId)
                .map(attr -> Boolean.TRUE.equals(attr.isUnique()))
                .orElse(false);
    }

    public long countActiveByPersonAndAttribute(Long personId, Long attributeId) {
        return personAttributeRepository.countActiveByPersonAndAttribute(personId, attributeId);
    }

    public boolean existsActiveDuplicateValue(Long personId, Long attributeId, String value) {
        return personAttributeRepository.existsActiveDuplicateValue(personId, attributeId, value);
    }

    public void deleteByIdAndPersonId(Long id, Long personId) {
        int deleted = personAttributeRepository.safeDeleteByIdAndPersonId(id, personId);
        if (deleted == 0) {
            // Contrainte “required/unique/dernier” ou pas trouvé/pas autorisé
            throw new RequiredAttributeDeletionException(
                    "Suppression interdite : attribut requis (dernier ou unique) ou non autorisé pour cette personne");
        }
    }

    public void updateValue(Long id, Long personId, String value) {
        personAttributeRepository.updateValue(id, personId, value);
        // si 0 ligne affectée : id inexistant ou pas à cette personne → à gérer au
        // service si besoin
    }

    public PersonAttribute createForPerson(Long personId, Long attributeId, String value) {
        var personRef = personRepository.getReferenceById(personId); // proxy sans SELECT
        var attributeRef = attributeRepository.getReferenceById(attributeId);

        var entity = new PersonAttributeEntity();
        entity.setPerson(personRef);
        entity.setAttribute(attributeRef);
        entity.setValue(value);
        entity.setValidFrom(LocalDateTime.now());
        entity.setValidTo(null);

        var saved = personAttributeRepository.save(entity);
        return personAttributeEntityMapper.toFullModel(saved);
    }

}
