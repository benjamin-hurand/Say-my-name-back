package com.saymyname.webapp.dto.course;

import com.saymyname.core.model.enums.DifficultyLevel;
import com.saymyname.core.model.enums.PoolType;

public record CourseQuestionDto(
        Long id,
        Integer questionRound,
        Long personId,
        String photoUrl,
        PoolType poolType,
        DifficultyLevel difficultyLevel) {

}