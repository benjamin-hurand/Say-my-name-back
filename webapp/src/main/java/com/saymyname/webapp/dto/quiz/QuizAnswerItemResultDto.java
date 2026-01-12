// src/main/java/com/saymyname/webapp/dto/quiz/QuizAnswerItemResultDto.java
package com.saymyname.webapp.dto.quiz;

import java.util.List;

import com.saymyname.core.model.enums.course.QuizQuestionItemRole;

public record QuizAnswerItemResultDto(
                Integer position,
                QuizQuestionItemRole role,
                Long knowledgeId,
                Long personId,
                Boolean correct,
                String userAnswerNormalized,
                String correctAnswer,
                List<ResultAttributeDto> resultAttributes) {
}
