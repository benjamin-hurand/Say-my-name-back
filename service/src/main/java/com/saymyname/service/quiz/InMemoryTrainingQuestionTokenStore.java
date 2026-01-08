// src/main/java/com/saymyname/service/quiz/InMemoryTrainingQuestionTokenStore.java
package com.saymyname.service.quiz;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;

@Component
public class InMemoryTrainingQuestionTokenStore implements TrainingQuestionTokenStore {

    private record Entry(Long userId, QuizQuestionSnapshot snapshot, Instant askedAt, Instant expiresAt) {
    }

    private final Map<String, Entry> map = new ConcurrentHashMap<>();

    @Override
    public String put(Long userId, QuizQuestionSnapshot snapshot, long ttlSeconds) {
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
        Instant now = Instant.now();
        map.put(token, new Entry(userId, snapshot, now, now.plusSeconds(ttlSeconds)));
        return token;
    }

    @Override
    public Optional<StoredTrainingQuestion> consume(String token, Long userId) {
        if (token == null || token.isBlank() || userId == null) {
            return Optional.empty();
        }

        // remove = atomique (et empêche la double-consommation)
        Entry e = map.remove(token);
        if (e == null) {
            return Optional.empty();
        }

        // expiration
        if (Instant.now().isAfter(e.expiresAt())) {
            return Optional.empty();
        }

        // user scoping (sécurité)
        if (!userId.equals(e.userId())) {
            // Option: on ne remet PAS l'entrée (anti-bruteforce / anti-vol),
            // sinon un attaquant peut essayer jusqu'au bon userId.
            return Optional.empty();
        }

        return Optional.of(new StoredTrainingQuestion(
                e.userId(),
                e.snapshot(),
                e.askedAt().toEpochMilli(),
                e.expiresAt().getEpochSecond()));
    }
}
