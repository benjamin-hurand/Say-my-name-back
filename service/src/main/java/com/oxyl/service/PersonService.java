package com.oxyl.service;

import com.oxyl.core.model.people.Person;
import com.oxyl.persistence.dao.PersonDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PersonService {
    private final PersonDao personDao;

    public PersonService(PersonDao personDao) {
        this.personDao = personDao;
    }

    public List<Person> findAll() {
        return personDao.findAll();
    }

    public List<Person> findAllWithoutUser() {
        return personDao.findAllWithoutUser();
    }

    public Optional<Person> findById(Long id) {
        return personDao.findById(id);
    }

    public List<Person> findByPromotionYear(Integer year) {
        return personDao.findByPromotionYear(year);
    }

//    public List<Person> findAllPersons() {
//        return personDao.findAllPersons();
//    }
//
//    public Person create(Person person) {
//        return personDao.create(person);
//    }

    // Add other methods as needed (e.g., findById, update, delete)
}
