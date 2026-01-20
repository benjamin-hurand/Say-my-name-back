// src/main/java/com/saymyname/webapp/dto/quiz/WordPuzzleStateDto.java
package com.saymyname.webapp.dto.quiz;

import java.util.List;

import com.saymyname.core.model.enums.quiz.LetterFeedback;

/**
 * DTO for WORD_PUZZLE current state (Wordle-style).
 * Contains attempt attempt with per-letter feedback.
 */
public record WordPuzzleStateDto(
                List<AttemptDto> attempts, // Attempt of all attempts
                Integer attemptsRemaining, // Remaining attempts
                Boolean solved, // True if solution found
                Boolean failed // True if max attempts reached without solving
) implements MultiStepStateDto {

        /**
         * Single attempt with word and per-letter feedback.
         */
        public record AttemptDto(
                        String word, // User's guess (canonicalized)
                        List<LetterFeedback> feedback, // Feedback per letter (EXACT/PRESENT/ABSENT)
                        Integer position // Attempt number (0-indexed)
        ) {
        }
}
