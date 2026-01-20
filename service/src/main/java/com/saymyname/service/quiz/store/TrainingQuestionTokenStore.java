// src/main/java/com/saymyname/service/quiz/TrainingQuestionTokenStore.java
package com.saymyname.service.quiz.store;

import java.util.Optional;

import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;

public interface TrainingQuestionTokenStore {

        /**
         * Contenu stocké derrière un token de training.
         * - userId: pour empêcher qu'un autre utilisateur consomme le token
         * - snapshot: vérité figée (audit-proof), sert à normaliser + valider
         * - askedAtEpochMs: timestamp de l'émission (fixé par le moteur)
         * - expiresAtEpochSec: pour expiration TTL
         * - rawSubmission / normalizedAudit: audit minimal multi-step (optionnel)
         */
        record StoredTrainingQuestion(
                        Long userId,
                        QuizQuestionSnapshot snapshot,
                        long askedAtEpochMs,
                        long expiresAtEpochSec,
                        String rawSubmission,
                        String normalizedAudit) {
        }

        /**
         * Crée un token pour un snapshot, scoped à un user.
         * askedAtEpochMs est fourni par le moteur (contrat figé).
         */
        String put(Long userId, QuizQuestionSnapshot snapshot, long askedAtEpochMs, long ttlSeconds);

        /**
         * Consomme le token (lecture + suppression atomique).
         * Le token ne peut être consommé que par le userId correspondant.
         */
        Optional<StoredTrainingQuestion> consume(String token, Long userId);

        /**
         * Lit le token SANS le supprimer (pour multi-step formats).
         * Le token ne peut être lu que par le userId correspondant.
         * Retourne empty si expiré ou non trouvé.
         */
        Optional<StoredTrainingQuestion> peek(String token, Long userId);

        /**
         * Met à jour le snapshot d'un token existant (pour multi-step formats).
         * Rafraîchit le TTL du token.
         */
        boolean update(String token, Long userId, QuizQuestionSnapshot updatedSnapshot);

        /**
         * ✅ Update atomique snapshot + audit (multi-step).
         * rawSubmission/normalizedAudit peuvent être null => on conserve l'existant.
         */
        boolean updateAttempt(String token, Long userId, QuizQuestionSnapshot updatedSnapshot, String rawSubmission,
                        String normalizedAudit);
}
