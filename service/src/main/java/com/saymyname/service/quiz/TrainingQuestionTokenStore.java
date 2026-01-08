// src/main/java/com/saymyname/service/quiz/TrainingQuestionTokenStore.java
package com.saymyname.service.quiz;

import java.util.Optional;

import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;

public interface TrainingQuestionTokenStore {

    /**
     * Contenu stocké derrière un token de training.
     * - userId: pour empêcher qu'un autre utilisateur consomme le token
     * - snapshot: vérité figée (audit-proof), sert à normaliser + valider
     * - expiresAtEpochSec: pour expiration TTL
     */
    record StoredTrainingQuestion(
            Long userId,
            QuizQuestionSnapshot snapshot,
            long expiresAtEpochSec) {
    }

    /**
     * Crée un token pour un snapshot, scoped à un user.
     */
    String put(Long userId, QuizQuestionSnapshot snapshot, long ttlSeconds);

    /**
     * Consomme le token (lecture + suppression atomique).
     * Le token ne peut être consommé que par le userId correspondant.
     */
    Optional<StoredTrainingQuestion> consume(String token, Long userId);
}
