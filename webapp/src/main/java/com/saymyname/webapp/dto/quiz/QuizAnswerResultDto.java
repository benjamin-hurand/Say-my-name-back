// src/main/java/com/saymyname/webapp/dto/quiz/QuizAnswerResultDto.java
package com.saymyname.webapp.dto.quiz;

import java.util.List;

import com.saymyname.webapp.dto.course.ResultAttributeDto;

public record QuizAnswerResultDto(
        boolean correct,
        String userAnswer,
        String correctAnswer,
        String feedbackMessage,
        List<ResultAttributeDto> resultAttributes) {
}
