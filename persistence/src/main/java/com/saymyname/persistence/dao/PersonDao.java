package com.saymyname.persistence.dao;

import com.saymyname.core.model.common.User;
import com.saymyname.core.model.game.options.GameOptions;
import com.saymyname.core.model.people.Person;
import com.saymyname.persistence.entity.PersonEntity;
import com.saymyname.persistence.mapper.PersonEntityMapper;
import com.saymyname.persistence.repository.PersonRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class PersonDao {
    private final PersonRepository personRepository;
    private final PersonEntityMapper personEntityMapper;

    public PersonDao(PersonRepository personRepository, PersonEntityMapper personEntityMapper) {
        this.personRepository = personRepository;
        this.personEntityMapper = personEntityMapper;
    }

    @Transactional
    public List<Person> findAll() {
        return personEntityMapper.toModelList(personRepository.findAll());
    }

    @Transactional
    public Optional<Person> findById(Long id) {
        Optional<PersonEntity> personEntity = personRepository.findById(id);
        return personEntity.map(personEntityMapper::toModel);
    }

    @Transactional
    public List<Person> findByOptions(GameOptions options) {
        return personEntityMapper.toModelList(personRepository.findByOptions(options));
    }

    @Transactional
    public Optional<Person> findByUser(User user) {
        Optional<PersonEntity> entityOpt = personRepository.findByUserId(user.getId());
        return entityOpt.map(personEntityMapper::toModel);
    }

}
