package com.saymyname.webapp.dto.quiz;

import java.util.List;

import com.saymyname.webapp.dto.course.ResultAttributeDto;

public record QuizResultDto(
        Boolean correct,
        Integer grade, // optionnel, v2

        String userAnswerLabel, // optionnel (utile MCQ)
        String correctAnswerLabel, // optionnel

        String feedbackMessage,

        List<ResultAttributeDto> resultAttributes,

        // multi-item formats
        List<ItemResultDto> itemResults,

        // follow-up
        QuizQuestionDto nextQuestion) {
    public record ItemResultDto(Long personId, Boolean correct, Integer grade) {
    }
}
