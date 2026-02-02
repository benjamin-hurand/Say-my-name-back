package com.saymyname.webapp.mapper.quiz;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.quiz.FormatMode;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.webapp.dto.quiz.QuizQuestionRequestDto;

@Component
public class QuizQuestionRequestDtoMapper {

    public FormatMode toFormatMode(QuizQuestionRequestDto req) {
        return req != null ? req.formatMode() : null;
    }

    public QuizFormat toForcedFormat(QuizQuestionRequestDto req) {
        return req != null ? req.format() : null;
    }

    public Boolean toTimed(QuizQuestionRequestDto req) {
        return req != null ? req.timed() : null;
    }

    public Integer toTimeLimitMs(QuizQuestionRequestDto req) {
        return req != null ? req.timeLimitMs() : null;
    }
}
