package com.saymyname.persistence.dao;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.persistence.mapper.PersonAttributeEntityMapper;
import com.saymyname.persistence.repository.PersonAttributeRepository;

@Repository
@Transactional
public class PersonAttributeDao {

    private final PersonAttributeRepository personAttributeRepository;
    private final PersonAttributeEntityMapper personAttributeEntityMapper;

    public PersonAttributeDao(PersonAttributeRepository personAttributeRepository,
            PersonAttributeEntityMapper personAttributeEntityMapper) {
        this.personAttributeRepository = personAttributeRepository;
        this.personAttributeEntityMapper = personAttributeEntityMapper;
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

}
