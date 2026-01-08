// src/main/java/com/saymyname/webapp/dto/quiz/QuizAnswerRequestDto.java
package com.saymyname.webapp.dto.quiz;

import com.saymyname.core.model.quiz.QuizAnswerSubmission;

public record QuizAnswerRequestDto(
        String questionToken,
        QuizAnswerSubmission submission,
        Boolean helpUsed) {
}
