package com.oxyl.persistence.dao;

import com.oxyl.core.model.people.Person;
import com.oxyl.persistence.entity.PersonEntity;
import com.oxyl.persistence.mapper.PersonBasicDtoMapper;
import com.oxyl.persistence.mapper.PersonEntityMapper;
import com.oxyl.persistence.repository.PersonRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class PersonDao {
    private final PersonRepository personRepository;
    private final PersonEntityMapper personEntityMapper;
    private final PersonBasicDtoMapper personBasicDtoMapper;

    public PersonDao(PersonRepository personRepository, PersonEntityMapper personEntityMapper, PersonBasicDtoMapper personBasicDtoMapper) {
        this.personRepository = personRepository;
        this.personEntityMapper = personEntityMapper;
        this.personBasicDtoMapper = personBasicDtoMapper;
    }

    @Transactional
    public List<Person> findAll() {
        return personEntityMapper.toGameModelList(personRepository.findAll());
    }

    @Transactional(readOnly = true)
    public List<Person> findAllWithoutUser() {
        return personBasicDtoMapper.toGameModelList(personRepository.findPersonsBasicInfoWithoutUser());
    }

    @Transactional
    public Optional<Person> findById(Long id) {
        Optional<PersonEntity> personEntity = personRepository.findById(id);
        return personEntity.map(personEntityMapper::toGameModel);
    }

    @Transactional
    public List<Person> findByPromotionYear(Integer year) {
        return personEntityMapper.toGameModelList(personRepository.findByPromotionYear(year));
    }

}
