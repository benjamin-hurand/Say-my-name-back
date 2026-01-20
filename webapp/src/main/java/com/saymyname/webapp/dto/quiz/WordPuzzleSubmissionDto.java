// src/main/java/com/saymyname/webapp/dto/quiz/WordPuzzleSubmissionDto.java
package com.saymyname.webapp.dto.quiz;

/**
 * DTO for WORD_PUZZLE submission (Wordle-style word guessing).
 */
public record WordPuzzleSubmissionDto(
    String word  // Full word guess (required, length validated server-side)
) {}
