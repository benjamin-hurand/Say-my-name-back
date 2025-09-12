package com.saymyname.persistence.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.game.options.GameOptions;
import com.saymyname.core.model.people.Person;
import com.saymyname.persistence.entity.PersonEntity;
import com.saymyname.persistence.mapper.PersonEntityMapper;
import com.saymyname.persistence.repository.PersonRepository;

@Repository
public class PersonDao {
    private final PersonRepository personRepository;
    private final PersonEntityMapper personEntityMapper;

    public PersonDao(PersonRepository personRepository,
            PersonEntityMapper personEntityMapper) {
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

    @Transactional(readOnly = true)
    public Optional<Long> findPersonIdByUserId(Long userId) {
        return personRepository.findIdByUserId(userId);
    }

    // Chaque "preload" exige une transaction existante (celle du service)
    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    public void preloadAttributesGraph(Long personId) {
        // Charge p + attributes + attribute (ManyToOne) dans le PC
        personRepository.fetchAttributesGraph(personId);
    }

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    public void preloadPhotos(Long personId) {
        personRepository.fetchPhotos(personId);
    }

    @Transactional(propagation = Propagation.MANDATORY, readOnly = true)
    public Optional<Person> mapManagedToModel(Long personId) {
        // Récupère l’ENTITY managée dans le PC et mappe → Model
        Optional<PersonEntity> pOpt = personRepository.findById(personId);
        return pOpt.map(personEntityMapper::toModel);
    }
}
