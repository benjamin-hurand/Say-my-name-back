package com.saymyname.service;

import com.saymyname.core.model.common.User;
import com.saymyname.core.model.people.Person;
import com.saymyname.persistence.dao.PersonDao;

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

    public Optional<Person> findById(Long id) {
        return personDao.findById(id);
    }

    public Optional<Person> getPersonByUser(User user) {
        return personDao.findByUser(user);
    }
}
