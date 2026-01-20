// src/main/java/com/saymyname/service/quiz/store/TokenTrainingAttemptStore.java
package com.saymyname.service.quiz.store;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.quiz.QuizQuestionSource;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;

@Component
public class TokenTrainingAttemptStore implements QuizAttemptStore {

        private final TrainingQuestionTokenStore tokenStore;

        public TokenTrainingAttemptStore(TrainingQuestionTokenStore tokenStore) {
                this.tokenStore = tokenStore;
        }

        @Override
        public AttemptHandle put(
                        QuizQuestionSource source,
                        Long userId,
                        QuizQuestionSnapshot snapshot,
                        long askedAtEpochMs,
                        Long ttlSeconds) {

                if (source == null)
                        throw new IllegalArgumentException("source is required");
                if (source == QuizQuestionSource.COURSE) {
                        throw new UnsupportedOperationException("TokenTrainingAttemptStore does not support COURSE");
                }
                if (ttlSeconds == null || ttlSeconds <= 0) {
                        throw new IllegalArgumentException("ttlSeconds must be > 0 for token store");
                }

                String token = tokenStore.put(userId, snapshot, askedAtEpochMs, ttlSeconds);
                return new AttemptHandle.TokenHandle(token);
        }

        @Override
        public Optional<StoredAttempt> peek(QuizQuestionSource source, AttemptHandle handle, Long userId) {
                if (source == null)
                        throw new IllegalArgumentException("source is required");
                if (!(handle instanceof AttemptHandle.TokenHandle th)) {
                        throw new IllegalArgumentException("Expected TokenHandle for token store");
                }
                return tokenStore.peek(th.value(), userId)
                                .map(s -> new StoredAttempt(
                                                s.userId(),
                                                s.snapshot(),
                                                s.askedAtEpochMs(),
                                                s.expiresAtEpochSec(),
                                                s.rawSubmission(),
                                                s.normalizedAudit()));
        }

        @Override
        public Optional<StoredAttempt> consume(QuizQuestionSource source, AttemptHandle handle, Long userId) {
                if (source == null)
                        throw new IllegalArgumentException("source is required");
                if (!(handle instanceof AttemptHandle.TokenHandle th)) {
                        throw new IllegalArgumentException("Expected TokenHandle for token store");
                }
                return tokenStore.consume(th.value(), userId)
                                .map(s -> new StoredAttempt(
                                                s.userId(),
                                                s.snapshot(),
                                                s.askedAtEpochMs(),
                                                s.expiresAtEpochSec(),
                                                s.rawSubmission(),
                                                s.normalizedAudit()));
        }

        @Override
        public boolean updateSnapshot(QuizQuestionSource source, AttemptHandle handle, Long userId,
                        QuizQuestionSnapshot updatedSnapshot) {
                if (source == null)
                        throw new IllegalArgumentException("source is required");
                if (!(handle instanceof AttemptHandle.TokenHandle th)) {
                        throw new IllegalArgumentException("Expected TokenHandle for token store");
                }
                return tokenStore.update(th.value(), userId, updatedSnapshot);
        }

        @Override
        public boolean updateAttempt(QuizQuestionSource source, AttemptHandle handle, Long userId,
                        QuizQuestionSnapshot updatedSnapshot, String rawSubmission, String normalizedAudit) {
                if (source == null)
                        throw new IllegalArgumentException("source is required");
                if (!(handle instanceof AttemptHandle.TokenHandle th)) {
                        throw new IllegalArgumentException("Expected TokenHandle for token store");
                }
                return tokenStore.updateAttempt(th.value(), userId, updatedSnapshot, rawSubmission, normalizedAudit);
        }
}
