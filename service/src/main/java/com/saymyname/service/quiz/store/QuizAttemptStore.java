// src/main/java/com/saymyname/service/quiz/store/QuizAttemptStore.java
package com.saymyname.service.quiz.store;

import java.time.ZoneId;
import java.util.Optional;

import com.saymyname.core.model.enums.quiz.QuizQuestionSource;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;

public interface QuizAttemptStore {

    sealed interface AttemptHandle permits AttemptHandle.TokenHandle, AttemptHandle.DbIdHandle {
        record TokenHandle(String value) implements AttemptHandle {
            public TokenHandle {
                if (value == null || value.isBlank()) {
                    throw new IllegalArgumentException("token handle cannot be null/blank");
                }
            }
        }

        record DbIdHandle(Long value) implements AttemptHandle {
            public DbIdHandle {
                if (value == null || value <= 0) {
                    throw new IllegalArgumentException("db id handle cannot be null/<=0");
                }
            }
        }
    }

    record StoredAttempt(
            Long userId,
            QuizQuestionSnapshot snapshot,
            long askedAtEpochMs,
            Long expiresAtEpochSec, // null pour DB (course)
            String rawSubmission,
            String normalizedAudit) {
    }

    AttemptHandle put(
            QuizQuestionSource source,
            Long userId,
            QuizQuestionSnapshot snapshot,
            long askedAtEpochMs,
            Long ttlSeconds);

    Optional<StoredAttempt> peek(
            QuizQuestionSource source,
            AttemptHandle handle,
            Long userId);

    Optional<StoredAttempt> consume(
            QuizQuestionSource source,
            AttemptHandle handle,
            Long userId);

    boolean updateSnapshot(
            QuizQuestionSource source,
            AttemptHandle handle,
            Long userId,
            QuizQuestionSnapshot updatedSnapshot);

    /**
     * ✅ Update atomique snapshot + audit (multi-step).
     * rawSubmission/normalizedAudit peuvent être null => conserver l'existant.
     */
    boolean updateAttempt(
            QuizQuestionSource source,
            AttemptHandle handle,
            Long userId,
            QuizQuestionSnapshot updatedSnapshot,
            String rawSubmission,
            String normalizedAudit);

    static long toEpochMs(java.time.LocalDateTime ldt) {
        if (ldt == null)
            return 0L;
        return ldt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}
