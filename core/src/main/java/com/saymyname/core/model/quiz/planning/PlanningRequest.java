// core/model/quiz/planning/PlanningRequest.java
package com.saymyname.core.model.quiz.planning;

import java.util.Objects;

import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.enums.quiz.FormatMode;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.quiz.candidate.EligibilityStats;
import com.saymyname.core.model.quiz.options.GameMode;

/**
 * Input to FormatPlanner: all info needed to decide format.
 *
 * Contains the formatMode (AUTO/FORCED), eligibility counts,
 * and gameMode context for the planner to make a decision.
 */
public record PlanningRequest(
        FormatMode formatMode,
        QuizFormat forcedFormat,
        EligibilityStats eligibility,
        GameMode gameMode,
        KnowledgeStatus knowledgeStatus, // nullable: null for TRAINING mode, set for COURSE mode
        Boolean timed,
        Integer timeLimitMs) {

    public PlanningRequest {
        Objects.requireNonNull(formatMode, "formatMode is required");
        Objects.requireNonNull(eligibility, "eligibility is required");
        Objects.requireNonNull(gameMode, "gameMode is required");

        if (formatMode == FormatMode.FORCED && forcedFormat == null) {
            throw new IllegalArgumentException("forcedFormat is required when formatMode=FORCED");
        }
        if (formatMode == FormatMode.AUTO && forcedFormat != null) {
            throw new IllegalArgumentException("forcedFormat must be null when formatMode=AUTO");
        }
    }

    /**
     * Create a request for FORCED format mode (TRAINING - no knowledge status).
     */
    public static PlanningRequest forced(
            QuizFormat format,
            EligibilityStats eligibility,
            GameMode gameMode,
            Boolean timed,
            Integer timeLimitMs) {
        return new PlanningRequest(FormatMode.FORCED, format, eligibility, gameMode, null, timed, timeLimitMs);
    }

    /**
     * Create a request for AUTO format mode (TRAINING - no knowledge status).
     */
    public static PlanningRequest auto(
            EligibilityStats eligibility,
            GameMode gameMode,
            Boolean timed,
            Integer timeLimitMs) {
        return new PlanningRequest(FormatMode.AUTO, null, eligibility, gameMode, null, timed, timeLimitMs);
    }

    /**
     * Create a request for AUTO format mode with knowledge status (COURSE mode).
     * The knowledgeStatus enables ladder-based format selection (D1).
     */
    public static PlanningRequest courseAuto(
            EligibilityStats eligibility,
            GameMode gameMode,
            KnowledgeStatus knowledgeStatus,
            Boolean timed,
            Integer timeLimitMs) {
        return new PlanningRequest(FormatMode.AUTO, null, eligibility, gameMode, knowledgeStatus, timed, timeLimitMs);
    }

    public boolean isForced() {
        return formatMode == FormatMode.FORCED;
    }

    public boolean isAuto() {
        return formatMode == FormatMode.AUTO;
    }
}
