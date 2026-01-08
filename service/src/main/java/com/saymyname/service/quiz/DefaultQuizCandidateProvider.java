// src/main/java/com/saymyname/service/quiz/DefaultQuizCandidateProvider.java
package com.saymyname.service.quiz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.quiz.options.GameOptions;
import com.saymyname.persistence.dao.PersonDao;

@Service
public class DefaultQuizCandidateProvider implements QuizCandidateProvider {

    private final PersonDao personDao;
    private final Random rnd = new Random();

    public DefaultQuizCandidateProvider(PersonDao personDao) {
        this.personDao = personDao;
    }

    @Override
    public List<Person> candidates(GameOptions options, Long userId, int limit) {
        List<Person> list = personDao.findByOptions(options, userId);
        if (list.size() <= limit)
            return list;
        return list.subList(0, limit);
    }

    @Override
    public List<Person> distractors(GameOptions options, Long userId, Long excludePersonId, int count) {
        List<Person> all = new ArrayList<>(personDao.findByOptions(options, userId));
        all.removeIf(p -> p.getId() != null && p.getId().equals(excludePersonId));
        Collections.shuffle(all, rnd);
        if (all.size() <= count)
            return all;
        return all.subList(0, count);
    }
}
