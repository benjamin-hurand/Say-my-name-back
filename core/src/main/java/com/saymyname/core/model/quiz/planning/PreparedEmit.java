// src/main/java/com/saymyname/core/model/quiz/planning/PreparedEmit.java
package com.saymyname.core.model.quiz.planning;

import java.util.Objects;

import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.quiz.QuizQuestionSpec;

/**
 * Contrat FINAL entre orchestration et engine.
 * QuizEngine ne connaît ni Course ni Training : il prend Spec + format + TTL.
 */
public record PreparedEmit(
        QuizFormat format,
        QuizQuestionSpec spec,
        Long ttlSeconds // null pour DB (course), sinon TTL token (training)
) {
    public PreparedEmit {
        Objects.requireNonNull(format, "format");
        Objects.requireNonNull(spec, "spec");
        // ttlSeconds peut être null (store DB) ou >0 (token store)
        if (ttlSeconds != null && ttlSeconds <= 0) {
            throw new IllegalArgumentException("ttlSeconds must be null or > 0");
        }
    }
}
