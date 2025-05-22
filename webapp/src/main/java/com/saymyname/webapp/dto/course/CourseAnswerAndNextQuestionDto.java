package com.saymyname.webapp.dto.course;

import java.util.List;

public record CourseAnswerAndNextQuestionDto(
        Boolean correct,
        String userAnswer,
        String correctAnswer,
        String feedbackMessage,
        CourseQuestionDto nextQuestion,
        List<ResultAttributeDto> resultAttributes,
        StatusCountsDto statusCounts) {
}
