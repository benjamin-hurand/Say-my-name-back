// src/main/java/com/saymyname/webapp/mapper/quiz/QuizQuestionsRequestDtoMapper.java
package com.saymyname.webapp.mapper.quiz;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.quiz.QuizPreferredFormat;
import com.saymyname.core.model.quiz.options.GameOptions;
import com.saymyname.webapp.dto.ReducedGameOptionsDto;
import com.saymyname.webapp.dto.quiz.QuizNextRequestDto;
import com.saymyname.webapp.mapper.ReducedGameOptionsDtoMapper;

@Component
public class QuizNextRequestDtoMapper {

    private final ReducedGameOptionsDtoMapper gameOptionsDtoMapper;

    public QuizNextRequestDtoMapper(ReducedGameOptionsDtoMapper gameOptionsDtoMapper) {
        this.gameOptionsDtoMapper = gameOptionsDtoMapper;
    }

    public QuizPreferredFormat toPreferredFormat(QuizNextRequestDto req) {
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

    public Boolean toTimed(QuizNextRequestDto req) {
        if (req == null)
            return null;
        return req.timed();
    }

    public Integer toTimeLimitMs(QuizNextRequestDto req) {
        if (req == null)
            return null;
        return req.timeLimitMs();
    }

    public GameOptions toGameOptions(QuizNextRequestDto req) {
        if (req == null || req.options() == null)
            return null;
        ReducedGameOptionsDto optionsDto = req.options();
        GameOptions options = gameOptionsDtoMapper.toModel(optionsDto);
        return options;
    }
}
