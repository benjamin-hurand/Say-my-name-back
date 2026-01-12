// src/main/java/com/saymyname/webapp/dto/quiz/QuizAnswerResultBaseDto.java
package com.saymyname.webapp.dto.quiz;

import java.util.List;

public record QuizAnswerResultBaseDto(
        boolean correct,
        String feedbackMessage,
        QuizQuestionDto nextQuestion,
        List<QuizAnswerItemResultDto> itemResults) {
}
