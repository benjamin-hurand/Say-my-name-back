// src/main/java/com/saymyname/webapp/mapper/quiz/QuizQuestionsRequestDtoMapper.java
package com.saymyname.webapp.mapper.quiz;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.quiz.QuizPreferredFormat;
import com.saymyname.webapp.dto.quiz.QuizQuestionsRequestDto;

@Component
public class QuizQuestionsRequestDtoMapper {

    public QuizPreferredFormat toPreferredFormat(QuizQuestionsRequestDto req) {
        if (req == null || req.preferredFormat() == null || req.preferredFormat().isBlank()) {
            return QuizPreferredFormat.AUTO;
        }
        String raw = req.preferredFormat().trim().toUpperCase();
        try {
            return QuizPreferredFormat.valueOf(raw);
        } catch (IllegalArgumentException ex) {
            // On ne casse pas la requête: fallback safe
            return QuizPreferredFormat.AUTO;
        }
    }

    public Integer toLimit(QuizQuestionsRequestDto req) {
        if (req == null)
            return null;
        return req.limit(); // suppose que ton dto a limit()
    }

    public Boolean toTimed(QuizQuestionsRequestDto req) {
        if (req == null)
            return null;
        return req.timed();
    }

    public Integer toTimeLimitMs(QuizQuestionsRequestDto req) {
        if (req == null)
            return null;
        return req.timeLimitMs();
    }
}
