// src/main/java/com/saymyname/core/model/enums/quiz/LetterFeedback.java
package com.saymyname.core.model.enums.quiz;

/**
 * Feedback for individual letters in WORD_PUZZLE format.
 * Similar to Wordle color coding system.
 */
public enum LetterFeedback {
    EXACT,    // Correct letter in correct position (green in Wordle)
    PRESENT,  // Correct letter in wrong position (yellow in Wordle)
    ABSENT    // Letter not in the word (gray in Wordle)
}
