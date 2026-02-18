package com.saymyname.core.model.quiz.snapshot;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class HangmanRules {
    Integer maxErrors;
    Boolean normalized;
    Boolean canSolveWholeWord;
    @Builder.Default
    List<String> alphabet = List.of();

    public void validateInvariants() {
        if (maxErrors == null || maxErrors <= 0) {
            throw new IllegalStateException("HangmanRules.maxErrors must be >= 1");
        }
        if (alphabet == null) {
            throw new IllegalStateException("HangmanRules.alphabet cannot be null");
        }
    }
}
