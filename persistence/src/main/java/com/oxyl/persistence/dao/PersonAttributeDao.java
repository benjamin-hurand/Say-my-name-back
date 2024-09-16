package com.oxyl.persistence.dao;

import com.oxyl.core.model.people.PersonAttribute;
import com.oxyl.persistence.entity.PersonAttributeEntity;
import com.oxyl.persistence.mapper.PersonAttributeEntityMapper;
import com.oxyl.persistence.repository.PersonAttributeRepository;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class PersonAttributeDao {

    private final PersonAttributeRepository personAttributeRepository;
    private final PersonAttributeEntityMapper personAttributeEntityMapper;

    public PersonAttributeDao(PersonAttributeRepository personAttributeRepository, PersonAttributeEntityMapper personAttributeEntityMapper) {
        this.personAttributeRepository = personAttributeRepository;
        this.personAttributeEntityMapper = personAttributeEntityMapper;
    }

    public List<PersonAttribute> findAttributesByPhotoId (Long photoId) {
        return personAttributeRepository
                .findAttributesByPhotoId(photoId)
                .stream()
                .map(personAttributeEntityMapper::toFullModel)
                .toList();
    }
}
