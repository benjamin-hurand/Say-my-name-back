// src/main/java/com/saymyname/webapp/dto/quiz/QuizAnswerSubmissionDto.java
package com.saymyname.webapp.dto.quiz;

import java.util.List;

public record QuizAnswerSubmissionDto(
        String userAnswer,
        Long selectedChoiceId,
        List<Long> selectedChoiceIds,
        Boolean swipeRight,
        List<Long> orderingIds,
        List<QuizAssociationPairDto> pairs,
        Integer timeMs) {
}
