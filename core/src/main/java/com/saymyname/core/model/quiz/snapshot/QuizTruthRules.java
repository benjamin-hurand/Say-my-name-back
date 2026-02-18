package com.saymyname.core.model.quiz.snapshot;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class QuizTruthRules {
    String operator;
    @Builder.Default
    boolean ignoreCase = true;
    @Builder.Default
    boolean ignoreDiacritics = true;
    @Builder.Default
    boolean trim = true;

    public void validateInvariants() {
        // no-op for now
    }
}
