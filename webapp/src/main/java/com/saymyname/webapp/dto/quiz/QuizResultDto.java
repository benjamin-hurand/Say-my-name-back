package com.saymyname.webapp.dto.quiz;

import java.util.List;

public record QuizResultDto(
        Boolean correct,
        Integer grade, // optionnel, v2

        String userAnswerLabel, // optionnel (utile MCQ)
        String correctAnswerLabel, // optionnel

        String feedbackMessage,

        List<TargetAnswerResultDto> targetAnswerResult,

        // multi-item formats
        List<ItemResultDto> itemResults,

        // follow-up
        QuizQuestionDto nextQuestion) {
    public record ItemResultDto(Long personId, Boolean correct, Integer grade) {
    }
}
