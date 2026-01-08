// src/main/java/com/saymyname/webapp/dto/quiz/QuizQuestionDto.java
package com.saymyname.webapp.dto.quiz;

import java.util.List;
import com.saymyname.core.model.enums.quiz.QuizFormat;

public record QuizQuestionDto(
        String questionToken, // ✅ NEW
        Long personId,
        String photoUrl,
        Long gameModeId,
        List<Long> targetAttributeIds,
        String operator,
        QuizQuestionContextDto context,
        QuizFormat format,
        QuizQuestionPayloadDto payload,
        QuizQuestionHintsDto hints,
        QuizQuestionDisplayDto display,
        QuizFollowUpDto followUp) {
}
