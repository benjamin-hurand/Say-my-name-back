// src/main/java/com/saymyname/service/QuizService.java
package com.saymyname.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.saymyname.core.model.enums.PhotoStatus;
import com.saymyname.core.model.enums.quiz.QuizPreferredFormat;
import com.saymyname.core.model.enums.quiz.QuizQuestionSource;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.quiz.QuizEntry;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.QuizQuestionContext;
import com.saymyname.core.model.quiz.QuizQuestionSpec;
import com.saymyname.core.model.quiz.options.GameOptions;
import com.saymyname.core.util.InitialCrafter;
import com.saymyname.persistence.dao.PersonDao;
import com.saymyname.service.quiz.QuizQuestionFactory;

@Service
public class QuizService {

    private final PersonDao personDao;
    private final InitialCrafter initialCrafter;
    private final QuizQuestionFactory quizQuestionFactory;

    public QuizService(
            PersonDao personDao,
            InitialCrafter initialCrafter,
            QuizQuestionFactory quizQuestionFactory) {
        this.personDao = personDao;
        this.initialCrafter = initialCrafter;
        this.quizQuestionFactory = quizQuestionFactory;
    }

    /** (Ancien) Training list “simple” — compat */
    public List<QuizEntry> getQuizEntries(GameOptions options, Long userId) {
        Objects.requireNonNull(options, "options");
        Objects.requireNonNull(options.getGameMode(), "options.gameMode");

        List<Person> persons = personDao.findByOptions(options, userId);

        if (options.getSortBy() != null && !options.getSortBy().isEmpty()) {
            persons.sort((p1, p2) -> {
                for (var sort : options.getSortBy()) {
                    String val1 = getAttributeValueFor(p1, sort.getAttribute().getId());
                    String val2 = getAttributeValueFor(p2, sort.getAttribute().getId());
                    int cmp = val1.compareToIgnoreCase(val2);
                    if (cmp != 0) {
                        return "ASC".equalsIgnoreCase(sort.getOrder()) ? cmp : -cmp;
                    }
                }
                return 0;
            });
        }

        return persons.stream()
                .map(p -> {
                    String initials = initialCrafter.computeInitials(p, options.getGameMode());
                    String storageKey = approvedStorageKeyOrThrow(p);

                    return new QuizEntry.Builder()
                            .withPersonId(p.getId())
                            .withStorageKey(storageKey)
                            .withInitials(initials)
                            .build();
                })
                .toList();
    }

    /**
     * TRAINING questions (plugin-based):
     * - construit un QuizQuestionSpec (core model)
     * - délègue à QuizQuestionFactory -> plugin
     */
    public List<QuizQuestion> getQuestions(GameOptions options, Long userId, QuizPreferredFormat preferredFormat) {
        Objects.requireNonNull(options, "options");
        Objects.requireNonNull(options.getGameMode(), "options.gameMode");

        List<Person> persons = personDao.findByOptions(options, userId);

        // cible = attributs du gameMode (calculé une fois)
        final List<Long> targetAttributeIds = options.getGameMode().getGameModeAttributes().stream()
                .map(gma -> gma.getAttribute().getId())
                .toList();

        final String operator = options.getGameMode().getOperator();

        return persons.stream()
                .map(person -> buildTrainingQuestion(options, person, targetAttributeIds, operator, preferredFormat))
                .toList();
    }

    private QuizQuestion buildTrainingQuestion(
            GameOptions options,
            Person person,
            List<Long> targetAttributeIds,
            String operator,
            QuizPreferredFormat preferredFormat) {

        String storageKey = approvedStorageKeyOrThrow(person);
        String initials = initialCrafter.computeInitials(person, options.getGameMode());

        QuizQuestionContext ctx = new QuizQuestionContext.Builder()
                .withSource(QuizQuestionSource.TRAINING)
                .withReducedOptionsId(options.getId())
                .build();

        QuizQuestionSpec spec = new QuizQuestionSpec.Builder()
                .withSource(QuizQuestionSource.TRAINING)
                .withPersonId(person.getId())
                .withStorageKey(storageKey) // Variante B (pragmatique) : ID de ressource, pas URL
                .withGameModeId(options.getGameMode().getId())
                .withTargetAttributeIds(targetAttributeIds)
                .withOperator(operator)
                .withContext(ctx)
                .withInitials(initials)
                .build();

        return quizQuestionFactory.build(spec, preferredFormat);
    }

    private static String approvedStorageKeyOrThrow(Person person) {
        return person.getPhotos().stream()
                .filter(p -> p.getStatus() == PhotoStatus.APPROVED)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Person " + person.getId() + " n'a pas de photo APPROVED malgré le filtre SQL"))
                .getStorageKey();
    }

    private String getAttributeValueFor(Person person, Long attributeId) {
        return person.getAttributes().stream()
                .filter(attr -> Objects.equals(attr.getAttribute().getId(), attributeId))
                .map(attr -> attr.getValue() != null ? attr.getValue() : "")
                .findFirst()
                .orElse("");
    }
}
