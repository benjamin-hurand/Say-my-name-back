// src/main/java/com/saymyname/service/quiz/QuizCandidateProvider.java
package com.saymyname.service.quiz;

import java.util.List;

import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.quiz.options.TrainingOptions;

public interface QuizCandidateProvider {
    List<Person> candidates(TrainingOptions options, Long userId, int limit);

    /**
     * Return random-ish distractors excluding personId. You can implement via SQL
     * later.
     */
    List<Person> distractors(TrainingOptions options, Long userId, Long excludePersonId, int count);
}
