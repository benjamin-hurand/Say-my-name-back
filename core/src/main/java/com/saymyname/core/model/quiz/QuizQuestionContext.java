package com.saymyname.core.model.quiz;

import com.saymyname.core.model.enums.DifficultyLevel;
import com.saymyname.core.model.enums.PoolType;
import com.saymyname.core.model.enums.quiz.QuizQuestionSource;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class QuizQuestionContext {
    QuizQuestionSource source;
    Long courseId;
    Long courseQuestionId;
    Long knowledgeId;
    Integer questionRound;
    PoolType poolType;
    DifficultyLevel difficultyLevel;
    Long reducedOptionsId;
    Boolean knowledgeTracking;
}
