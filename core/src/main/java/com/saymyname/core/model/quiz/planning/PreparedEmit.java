package com.saymyname.core.model.quiz.planning;

import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.quiz.QuizQuestionSpec;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class PreparedEmit {
    QuizFormat format;
    QuizQuestionSpec spec;
    Long ttlSeconds;

    // Backward-compatible record-style accessors.
    public QuizFormat format() {
        return format;
    }

    public QuizQuestionSpec spec() {
        return spec;
    }

    public Long ttlSeconds() {
        return ttlSeconds;
    }
}
