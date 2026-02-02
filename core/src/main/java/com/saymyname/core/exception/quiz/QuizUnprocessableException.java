// src/main/java/com/saymyname/core/exception/quiz/QuizUnprocessableException.java
package com.saymyname.core.exception.quiz;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import com.saymyname.core.exception.common.ValidationException;

/**
 * 422 Unprocessable Entity, spécifique au domaine Quiz.
 * Front-friendly: errorCode stable + details structurés.
 */
public class QuizUnprocessableException extends ValidationException {

    public enum ErrorCode {
        NO_CANDIDATE,
        FORMAT_NOT_FEASIBLE,
        INSUFFICIENT_CHOICES_FOR_MCQ,
        MISSING_REQUIRED_PAYLOAD,
        CONSTRAINTS_IMPOSSIBLE
    }

    private final ErrorCode errorCode;
    private final Map<String, Object> details;

    public QuizUnprocessableException(ErrorCode errorCode, String message) {
        this(errorCode, message, null);
    }

    public QuizUnprocessableException(ErrorCode errorCode, String message, Map<String, Object> details) {
        super(message);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode");
        this.details = (details == null) ? Collections.emptyMap() : Collections.unmodifiableMap(details);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}
