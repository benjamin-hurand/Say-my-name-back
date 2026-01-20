// src/main/java/com/saymyname/core/model/quiz/answer/NormalizedWordPuzzle.java
package com.saymyname.core.model.quiz.answer;

/**
 * Normalized submission for WORD_PUZZLE format (Wordle-like).
 * Contains canonicalized full word guess.
 */
public record NormalizedWordPuzzle(
        String word // Canonicalized, uppercase
) implements NormalizedAudit {

    @Override
    public String auditString() {
        return "WORD:" + (word != null ? word : "null");
    }
}
