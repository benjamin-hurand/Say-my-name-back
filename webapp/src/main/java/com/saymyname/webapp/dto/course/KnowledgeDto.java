package com.saymyname.webapp.dto.course;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.webapp.dto.GameModeDto;
import com.saymyname.webapp.dto.ReducedPersonDto;
import com.saymyname.webapp.dto.ReducedUserDto;

public record KnowledgeDto(
        Long id,
        ReducedUserDto user,
        GameModeDto gameMode,
        ReducedPersonDto person,
        KnowledgeStatus status,
        LocalDateTime nextReviewDate,
        LocalDateTime lastReviewDate,
        Integer totalRepetitionCount,
        Integer failureCount,
        Integer successCount,
        Integer srsStreak,
        Integer globalStreak,
        BigDecimal easeFactor,
        Double difficulty,
        Double stability) {

}
