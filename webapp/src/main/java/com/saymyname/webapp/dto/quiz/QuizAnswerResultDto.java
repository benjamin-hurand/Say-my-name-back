package com.saymyname.webapp.dto.quiz;

import java.util.List;

public record QuizAnswerResultDto(
                Boolean correct,
                String feedbackMessage,
                QuizQuestionDto nextQuestion,
                List<QuizAnswerItemResultDto> itemResults) {
}
