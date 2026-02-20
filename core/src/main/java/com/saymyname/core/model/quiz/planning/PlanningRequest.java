package com.saymyname.core.model.quiz.planning;

import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.enums.quiz.FormatMode;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.quiz.candidate.EligibilityStats;
import com.saymyname.core.model.quiz.options.GameMode;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class PlanningRequest {
    FormatMode formatMode;
    QuizFormat forcedFormat;
    EligibilityStats eligibility;
    Long gameModeId;
    @Builder.Default
    List<Long> targetAttributeIds = List.of();
    String operator;
    KnowledgeStatus knowledgeStatus;
    Boolean timed;
    Integer timeLimitMs;

    public static PlanningRequest forced(
            QuizFormat format,
            EligibilityStats eligibility,
            Long gameModeId,
            List<Long> targetAttributeIds,
            String operator,
            Boolean timed,
            Integer timeLimitMs) {
        return PlanningRequest.builder()
                .formatMode(FormatMode.FORCED)
                .forcedFormat(format)
                .eligibility(eligibility)
                .gameModeId(gameModeId)
                .targetAttributeIds(targetAttributeIds == null ? List.of() : targetAttributeIds)
                .operator(operator)
                .timed(timed)
                .timeLimitMs(timeLimitMs)
                .build();
    }

    // Backward-compatible overload using legacy GameMode model.
    public static PlanningRequest forced(
            QuizFormat format,
            EligibilityStats eligibility,
            GameMode gameMode,
            Boolean timed,
            Integer timeLimitMs) {
        Long gameModeId = gameMode != null ? gameMode.getId() : null;
        List<Long> targetAttributeIds = gameMode != null && gameMode.getGameModeAttributes() != null
                ? gameMode.getGameModeAttributes().stream()
                        .map(gma -> gma != null ? gma.getAttributeId() : null)
                        .filter(id -> id != null)
                        .toList()
                : List.of();
        String operator = gameMode != null ? gameMode.getOperator() : null;
        return forced(format, eligibility, gameModeId, targetAttributeIds, operator, timed, timeLimitMs);
    }

    public static PlanningRequest auto(
            EligibilityStats eligibility,
            Long gameModeId,
            List<Long> targetAttributeIds,
            String operator,
            Boolean timed,
            Integer timeLimitMs) {
        return PlanningRequest.builder()
                .formatMode(FormatMode.AUTO)
                .eligibility(eligibility)
                .gameModeId(gameModeId)
                .targetAttributeIds(targetAttributeIds == null ? List.of() : targetAttributeIds)
                .operator(operator)
                .timed(timed)
                .timeLimitMs(timeLimitMs)
                .build();
    }

    // Backward-compatible overload using legacy GameMode model.
    public static PlanningRequest auto(
            EligibilityStats eligibility,
            GameMode gameMode,
            Boolean timed,
            Integer timeLimitMs) {
        Long gameModeId = gameMode != null ? gameMode.getId() : null;
        List<Long> targetAttributeIds = gameMode != null && gameMode.getGameModeAttributes() != null
                ? gameMode.getGameModeAttributes().stream()
                        .map(gma -> gma != null ? gma.getAttributeId() : null)
                        .filter(id -> id != null)
                        .toList()
                : List.of();
        String operator = gameMode != null ? gameMode.getOperator() : null;
        return auto(eligibility, gameModeId, targetAttributeIds, operator, timed, timeLimitMs);
    }

    public static PlanningRequest courseAuto(
            EligibilityStats eligibility,
            Long gameModeId,
            List<Long> targetAttributeIds,
            String operator,
            KnowledgeStatus knowledgeStatus,
            Boolean timed,
            Integer timeLimitMs) {
        return PlanningRequest.builder()
                .formatMode(FormatMode.AUTO)
                .eligibility(eligibility)
                .gameModeId(gameModeId)
                .targetAttributeIds(targetAttributeIds == null ? List.of() : targetAttributeIds)
                .operator(operator)
                .knowledgeStatus(knowledgeStatus)
                .timed(timed)
                .timeLimitMs(timeLimitMs)
                .build();
    }

    // Backward-compatible overload using legacy GameMode model.
    public static PlanningRequest courseAuto(
            EligibilityStats eligibility,
            GameMode gameMode,
            KnowledgeStatus knowledgeStatus,
            Boolean timed,
            Integer timeLimitMs) {
        Long gameModeId = gameMode != null ? gameMode.getId() : null;
        List<Long> targetAttributeIds = gameMode != null && gameMode.getGameModeAttributes() != null
                ? gameMode.getGameModeAttributes().stream()
                        .map(gma -> gma != null ? gma.getAttributeId() : null)
                        .filter(id -> id != null)
                        .toList()
                : List.of();
        String operator = gameMode != null ? gameMode.getOperator() : null;
        return courseAuto(eligibility, gameModeId, targetAttributeIds, operator, knowledgeStatus, timed, timeLimitMs);
    }

    public boolean isForced() {
        return formatMode == FormatMode.FORCED;
    }

    public boolean isAuto() {
        return formatMode == FormatMode.AUTO;
    }

    // Backward-compatible record-style accessors.
    public QuizFormat forcedFormat() {
        return forcedFormat;
    }

    public EligibilityStats eligibility() {
        return eligibility;
    }

    public KnowledgeStatus knowledgeStatus() {
        return knowledgeStatus;
    }
}
