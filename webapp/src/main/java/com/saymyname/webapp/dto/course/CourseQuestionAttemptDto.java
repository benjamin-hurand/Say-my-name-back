package com.saymyname.webapp.dto.course;

import java.time.LocalDateTime;

import com.saymyname.core.model.enums.PoolType;

public record CourseQuestionAttemptDto(
        Long id,
        CourseDto course,
        KnowledgeDto knowledge,
        Integer questionRound,
        LocalDateTime askedAt,
        LocalDateTime answeredAt,
        Integer responseTimeMs,
        String userAnswer,
        Boolean correct,
        PoolType poolType,
        Boolean helpUsed) {

}
