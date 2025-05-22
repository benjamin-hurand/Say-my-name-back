package com.saymyname.webapp.dto.course;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.webapp.dto.GameModeDto;
import com.saymyname.webapp.dto.PersonDto;
import com.saymyname.webapp.dto.UserDto;

public record KnowledgeDto(
        Long id,
        UserDto user,
        GameModeDto gameMode,
        PersonDto person,
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
