package com.oxyl.persistence.dao;

import com.oxyl.core.model.Person;
import com.oxyl.persistence.entity.PersonEntity;
import com.oxyl.persistence.mapper.PersonEntityMapper;
import com.oxyl.persistence.mapper.PromotionEntityMapper;
import com.oxyl.persistence.repository.PersonRepository;
import jakarta.persistence.EntityManager;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class PersonDao {
    private final EntityManager entityManager;
    private final PersonRepository personRepository;
    private final PersonEntityMapper personEntityMapper;
    private final PromotionEntityMapper promotionEntityMapper;

    public PersonDao(EntityManager entityManager, PersonRepository personRepository, PersonEntityMapper personEntityMapper, PromotionEntityMapper promotionEntityMapper) {
        this.entityManager = entityManager;
        this.personRepository = personRepository;
        this.personEntityMapper = personEntityMapper;
        this.promotionEntityMapper = promotionEntityMapper;
    }

    @Transactional
    public List<Person> findAll() {
        return personEntityMapper.toModelList(personRepository.findAll());
    }

    @Transactional
    public Optional<Person> findById(Long id) {
//        PersonEntity personEntity = entityManager.createQuery(
//                        "SELECT p FROM PersonEntity p LEFT JOIN FETCH p.promotions WHERE p.id = :id", PersonEntity.class)
//                .setParameter("id", id)
//                .getSingleResult();
        Optional<PersonEntity> personEntity = personRepository.findById(id);
        return personEntity.map(personEntityMapper::toModel);
    }

}
