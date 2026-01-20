// src/main/java/com/saymyname/webapp/dto/quiz/HangmanStateDto.java
package com.saymyname.webapp.dto.quiz;

import java.util.List;

/**
 * DTO for HANGMAN current state.
 */
public record HangmanStateDto(
                String mask,
                Integer maxErrors, // null if not provided (rules-based)
                Integer errorsCount,
                List<String> triedLetters,
                List<String> wrongLetters) implements MultiStepStateDto {
}
