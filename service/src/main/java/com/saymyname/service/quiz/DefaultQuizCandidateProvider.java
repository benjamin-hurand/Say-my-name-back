// src/main/java/com/saymyname/service/quiz/DefaultQuizCandidateProvider.java
package com.saymyname.service.quiz;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.quiz.options.GameOptions;
import com.saymyname.persistence.dao.PersonDao;

@Service
public class DefaultQuizCandidateProvider implements QuizCandidateProvider {

    private final PersonDao personDao;

    public DefaultQuizCandidateProvider(PersonDao personDao) {
        this.personDao = personDao;
    }

    @Override
    public List<Person> candidates(GameOptions options, Long userId, int limit) {
        List<Person> list = new ArrayList<>(personDao.findByOptions(options, userId));
        if (list.size() <= 1) {
            return list;
        }

        // Randomize to avoid deterministic "same first N persons" from DB order.
        Collections.shuffle(list, ThreadLocalRandom.current());

        if (limit <= 0 || list.size() <= limit) {
            return list;
        }
        return list.subList(0, limit);
    }

    @Override
    public List<Person> distractors(GameOptions options, Long userId, Long excludePersonId, int count) {
        List<Person> all = new ArrayList<>(personDao.findByOptions(options, userId));
        all.removeIf(p -> p != null && p.getId() != null && p.getId().equals(excludePersonId));

        if (all.size() <= 1) {
            return all;
        }

        Collections.shuffle(all, ThreadLocalRandom.current());

        if (count <= 0 || all.size() <= count) {
            return all;
        }
        return all.subList(0, count);
    }
}
