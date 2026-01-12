// src/main/java/com/saymyname/webapp/dto/quiz/TrainingAnswerResultDto.java
package com.saymyname.webapp.dto.quiz;

import java.util.List;

public record TrainingAnswerResultDto(
        boolean correct,
        String feedbackMessage,
        QuizQuestionDto nextQuestion,
        List<QuizAnswerItemResultDto> itemResults) {
}
