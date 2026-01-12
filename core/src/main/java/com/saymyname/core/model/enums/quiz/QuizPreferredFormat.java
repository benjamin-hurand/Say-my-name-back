// src/main/java/com/saymyname/core/model/enums/quiz/QuizPreferredFormat.java
package com.saymyname.core.model.enums.quiz;

/**
 * User's preferred quiz format.
 *
 * Note: SPEED is a MODIFIER, not a standalone format.
 * When SPEED is selected, the backend:
 * - Picks a fast format (MCQ or BINARY_SWIPE)
 * - Sets timed=true in QuizQuestionDisplay
 * - Sets appropriate timeLimitMs (e.g., 5s for MCQ, 3s for BINARY_SWIPE)
 */
public enum QuizPreferredFormat {
    AUTO,
    TEXT_INPUT,
    CLOZE,
    HANGMAN,
    MCQ,
    BINARY_SWIPE,
    ASSOCIATION,
    ORDERING,
    SPEED // Modifier: picks fast format + sets timed=true
}
