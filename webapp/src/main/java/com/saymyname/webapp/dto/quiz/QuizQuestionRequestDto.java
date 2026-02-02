package com.saymyname.webapp.dto.quiz;

import com.saymyname.core.model.enums.quiz.FormatMode;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.webapp.dto.TrainingOptionsDto;

/**
 * Contract: request to emit a question (training side).
 *
 * Rules (validated by controller):
 * - formatMode is required
 * - AUTO => format must be null
 * - FORCED => format is required
 * - timed=true => timeLimitMs required (>0)
 * - timed!=true => timeLimitMs must be null
 */
public record QuizQuestionRequestDto(
        TrainingOptionsDto options,
        FormatMode formatMode, // required: AUTO | FORCED
        QuizFormat format, // required if FORCED, must be null if AUTO
        Boolean timed,
        Integer timeLimitMs) {
}
