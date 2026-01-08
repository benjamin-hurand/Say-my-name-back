package com.saymyname.webapp.dto.quiz;

import com.saymyname.core.model.enums.PoolType;
import com.saymyname.core.model.enums.quiz.QuizQuestionSource;
import com.saymyname.core.model.enums.DifficultyLevel;

public record QuizQuestionContextDto(
                QuizQuestionSource source,

                // COURSE context
                Long courseId,
                Long courseQuestionId,
                Integer questionRound,
                PoolType poolType,
                DifficultyLevel difficultyLevel,

                // TRAINING context (optionnel)
                Long reducedOptionsId) {
}
