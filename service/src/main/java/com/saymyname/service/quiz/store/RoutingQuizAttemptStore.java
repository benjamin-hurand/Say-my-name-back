// src/main/java/com/saymyname/service/quiz/store/RoutingQuizAttemptStore.java
package com.saymyname.service.quiz.store;

import java.util.Objects;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.quiz.QuizQuestionSource;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.service.course.store.DbCourseAttemptStore;

/**
 * Routeur: choisit l'impl adaptée selon la source.
 * - TRAINING/REVIEW -> TokenTrainingAttemptStore
 * - COURSE -> DbCourseAttemptStore
 */
@Component
@Primary
public class RoutingQuizAttemptStore implements QuizAttemptStore {

    private final TokenTrainingAttemptStore trainingStore;
    private final DbCourseAttemptStore courseStore;

    public RoutingQuizAttemptStore(
            TokenTrainingAttemptStore trainingStore,
            DbCourseAttemptStore courseStore) {
        this.trainingStore = Objects.requireNonNull(trainingStore);
        this.courseStore = Objects.requireNonNull(courseStore);
    }

    private QuizAttemptStore resolve(QuizQuestionSource source) {
        if (source == null) {
            throw new IllegalArgumentException("source is required");
        }
        return switch (source) {
            case TRAINING, REVIEW -> trainingStore;
            case COURSE -> courseStore;
        };
    }

    @Override
    public AttemptHandle put(
            QuizQuestionSource source,
            Long userId,
            QuizQuestionSnapshot snapshot,
            long askedAtEpochMs,
            Long ttlSeconds) {
        return resolve(source).put(source, userId, snapshot, askedAtEpochMs, ttlSeconds);
    }

    @Override
    public Optional<StoredAttempt> peek(
            QuizQuestionSource source,
            AttemptHandle handle,
            Long userId) {
        return resolve(source).peek(source, handle, userId);
    }

    @Override
    public Optional<StoredAttempt> consume(
            QuizQuestionSource source,
            AttemptHandle handle,
            Long userId) {
        return resolve(source).consume(source, handle, userId);
    }

    @Override
    public boolean updateSnapshot(
            QuizQuestionSource source,
            AttemptHandle handle,
            Long userId,
            QuizQuestionSnapshot updatedSnapshot) {
        return resolve(source).updateSnapshot(source, handle, userId, updatedSnapshot);
    }

    @Override
    public boolean updateAttempt(
            QuizQuestionSource source,
            AttemptHandle handle,
            Long userId,
            QuizQuestionSnapshot updatedSnapshot,
            String rawSubmission,
            String normalizedAudit) {
        return resolve(source).updateAttempt(source, handle, userId, updatedSnapshot, rawSubmission, normalizedAudit);
    }
}
