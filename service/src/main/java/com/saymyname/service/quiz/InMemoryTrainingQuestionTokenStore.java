// src/main/java/com/saymyname/service/quiz/InMemoryTrainingQuestionTokenStore.java
package com.saymyname.service.quiz;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.service.quiz.store.TrainingQuestionTokenStore;

@Component
public class InMemoryTrainingQuestionTokenStore implements TrainingQuestionTokenStore {

    private record Entry(
            Long userId,
            QuizQuestionSnapshot snapshot,
            Instant askedAt,
            Instant expiresAt,
            long originalTtlSeconds,
            String rawSubmission,
            String normalizedAudit) {
    }

    private final Map<String, Entry> map = new ConcurrentHashMap<>();

    @Override
    public String put(Long userId, QuizQuestionSnapshot snapshot, long askedAtEpochMs, long ttlSeconds) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (snapshot == null) {
            throw new IllegalArgumentException("snapshot is required");
        }
        if (ttlSeconds <= 0) {
            throw new IllegalArgumentException("ttlSeconds must be > 0");
        }

        String token = UUID.randomUUID().toString();
        Instant askedAt = Instant.ofEpochMilli(askedAtEpochMs);
        Instant now = Instant.now();
        // NB: expiration à partir de now (pas askedAt) = comportement stable pour
        // refresh TTL.
        map.put(token, new Entry(userId, snapshot, askedAt, now.plusSeconds(ttlSeconds), ttlSeconds, null, null));
        return token;
    }

    @Override
    public Optional<StoredTrainingQuestion> consume(String token, Long userId) {
        if (token == null || token.isBlank() || userId == null) {
            return Optional.empty();
        }

        Entry e = map.remove(token);
        if (e == null) {
            return Optional.empty();
        }

        if (Instant.now().isAfter(e.expiresAt())) {
            return Optional.empty();
        }

        if (!userId.equals(e.userId())) {
            return Optional.empty();
        }

        return Optional.of(new StoredTrainingQuestion(
                e.userId(),
                e.snapshot(),
                e.askedAt().toEpochMilli(),
                e.expiresAt().getEpochSecond(),
                e.rawSubmission(),
                e.normalizedAudit()));
    }

    @Override
    public Optional<StoredTrainingQuestion> peek(String token, Long userId) {
        if (token == null || token.isBlank() || userId == null) {
            return Optional.empty();
        }

        Entry e = map.get(token);
        if (e == null) {
            return Optional.empty();
        }

        Instant now = Instant.now();
        if (now.isAfter(e.expiresAt())) {
            map.remove(token);
            return Optional.empty();
        }

        if (!userId.equals(e.userId())) {
            return Optional.empty();
        }

        return Optional.of(new StoredTrainingQuestion(
                e.userId(),
                e.snapshot(),
                e.askedAt().toEpochMilli(),
                e.expiresAt().getEpochSecond(),
                e.rawSubmission(),
                e.normalizedAudit()));
    }

    @Override
    public boolean update(String token, Long userId, QuizQuestionSnapshot updatedSnapshot) {
        return updateAttempt(token, userId, updatedSnapshot, null, null);
    }

    @Override
    public boolean updateAttempt(String token, Long userId, QuizQuestionSnapshot updatedSnapshot, String rawSubmission,
            String normalizedAudit) {

        if (token == null || token.isBlank() || userId == null || updatedSnapshot == null) {
            return false;
        }

        Entry updated = map.computeIfPresent(token, (k, oldEntry) -> {
            if (!userId.equals(oldEntry.userId())) {
                return oldEntry;
            }

            Instant now = Instant.now();
            if (now.isAfter(oldEntry.expiresAt())) {
                return null;
            }

            String raw = rawSubmission != null ? rawSubmission : oldEntry.rawSubmission();
            String norm = normalizedAudit != null ? normalizedAudit : oldEntry.normalizedAudit();

            return new Entry(
                    oldEntry.userId(),
                    updatedSnapshot,
                    oldEntry.askedAt(),
                    now.plusSeconds(oldEntry.originalTtlSeconds()),
                    oldEntry.originalTtlSeconds(),
                    raw,
                    norm);
        });

        return updated != null;
    }
}
