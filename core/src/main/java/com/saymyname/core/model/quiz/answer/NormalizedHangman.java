// src/main/java/com/saymyname/core/model/quiz/answer/NormalizedHangman.java
package com.saymyname.core.model.quiz.answer;

import com.saymyname.core.model.enums.quiz.HangmanAction;

/**
 * Normalized submission for HANGMAN format.
 * Contains canonicalized letter (GUESS) or full word (SOLVE).
 */
public record NormalizedHangman(
        HangmanAction action,
        String letter, // Canonicalized, uppercase (for GUESS)
        String value // Canonicalized full word (for SOLVE)
) implements NormalizedAudit {

    @Override
    public String auditString() {
        if (action == HangmanAction.GUESS) {
            return "GUESS:" + (letter != null ? letter : "null");
        } else if (action == HangmanAction.SOLVE) {
            return "SOLVE:" + (value != null ? value : "null");
        }
        return "INVALID";
    }
}
