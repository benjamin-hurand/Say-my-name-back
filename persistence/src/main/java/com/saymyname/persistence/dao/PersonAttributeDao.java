package com.saymyname.persistence.dao;

import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.persistence.entity.PersonAttributeEntity;
import com.saymyname.persistence.mapper.PersonAttributeEntityMapper;
import com.saymyname.persistence.repository.PersonAttributeRepository;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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

    public Long countPersonsMatchingFilter(String minValue, String nextValue, LocalDateTime seasonStart,
            Long attributeId) {
        return personAttributeRepository.countPersonsMatchingFilter(minValue, nextValue, seasonStart, attributeId);
    }

}
