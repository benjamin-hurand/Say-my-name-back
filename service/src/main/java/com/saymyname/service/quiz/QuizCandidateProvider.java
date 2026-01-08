// src/main/java/com/saymyname/service/quiz/QuizCandidateProvider.java
package com.saymyname.service.quiz;

import java.util.List;

import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.quiz.options.GameOptions;

public interface QuizCandidateProvider {
    List<Person> candidates(GameOptions options, Long userId, int limit);

    /**
     * Return random-ish distractors excluding personId. You can implement via SQL
     * later.
     */
    List<Person> distractors(GameOptions options, Long userId, Long excludePersonId, int count);
}
