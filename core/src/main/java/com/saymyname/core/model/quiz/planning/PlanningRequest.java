package com.saymyname.core.model.quiz.planning;

import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.enums.quiz.FormatMode;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.quiz.candidate.EligibilityStats;
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

    public boolean isForced() {
        return formatMode == FormatMode.FORCED;
    }

    public boolean isAuto() {
        return formatMode == FormatMode.AUTO;
    }
}
