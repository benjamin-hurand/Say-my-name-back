// src/main/java/com/saymyname/webapp/dto/quiz/QuizQuestionDto.java
package com.saymyname.webapp.dto.quiz;

import com.saymyname.core.model.enums.quiz.QuizFormat;

public record QuizQuestionDto(
        String questionHandle, // TRAINING: token ; COURSE: courseAttemptId
        Long gameModeId,

        QuizQuestionContextDto context, // source + ids course/training
        QuizFormat format, // single source of truth du type de question
        QuizQuestionPayloadDto payload, // tout ce qui sert à afficher et répondre

        QuizQuestionHintsDto hints, // indices optionnels
        QuizQuestionDisplayDto display, // copy + timing

        QuizFollowUpDto followUp, // optionnel
        String reasonCode, // explicabilité
        String reasonDetailsJson // debug/analytics
) {
}
