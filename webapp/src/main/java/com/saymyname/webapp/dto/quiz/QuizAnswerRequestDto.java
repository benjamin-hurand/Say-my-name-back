// src/main/java/com/saymyname/webapp/dto/quiz/QuizAnswerRequestDto.java
package com.saymyname.webapp.dto.quiz;

public record QuizAnswerRequestDto(
                String questionToken,
                QuizAnswerSubmissionDto submission,
                Boolean helpUsed) {
}
