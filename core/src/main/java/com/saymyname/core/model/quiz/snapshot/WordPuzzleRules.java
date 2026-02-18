package com.saymyname.core.model.quiz.snapshot;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class WordPuzzleRules {
    Integer maxAttempts;
    Integer wordLength;
    Boolean caseSensitive;
    Boolean allowRepeatedLetters;

    public void validateInvariants() {
        if (maxAttempts == null || maxAttempts < 1) {
            throw new IllegalStateException("WordPuzzleRules.maxAttempts must be >= 1");
        }
        if (wordLength == null || wordLength < 1) {
            throw new IllegalStateException("WordPuzzleRules.wordLength must be >= 1");
        }
    }
}
