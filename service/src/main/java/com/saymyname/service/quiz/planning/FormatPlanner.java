// service/quiz/planning/FormatPlanner.java
package com.saymyname.service.quiz.planning;

import org.springframework.stereotype.Service;

import com.saymyname.core.exception.quiz.QuizUnprocessableException;
import com.saymyname.core.model.enums.quiz.QuizDecisionReasonCode;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.quiz.candidate.EligibilityStats;
import com.saymyname.core.model.quiz.planning.PlanningDecision;
import com.saymyname.core.model.quiz.planning.PlanningRequest;

import java.util.Map;

/**
 * Decides optimal format (AUTO) or validates feasibility (FORCED).
 * Central location for format selection logic.
 */
@Service
public class FormatPlanner {

    /**
     * Plan the format based on the request.
     * - FORCED: validates feasibility and throws if impossible
     * - AUTO: chooses the best format based on eligibility
     *
     * @param request the planning request with eligibility stats
     * @return the planning decision with chosen format
     * @throws QuizUnprocessableException if FORCED format is impossible or AUTO has no candidates
     */
    public PlanningDecision plan(PlanningRequest request) {
        if (request.isForced()) {
            return planForced(request);
        } else {
            return planAuto(request);
        }
    }

    /**
     * Validate and return decision for FORCED format.
     */
    private PlanningDecision planForced(PlanningRequest request) {
        QuizFormat format = request.forcedFormat();
        EligibilityStats stats = request.eligibility();

        if (!isFeasible(format, stats)) {
            int minRequired = PlanningDecision.minCandidatesFor(format);
            boolean needsPhoto = PlanningDecision.formatRequiresPhoto(format);

            long available = needsPhoto ? stats.withApprovedPhoto() : stats.totalEligible();

            throw new QuizUnprocessableException(
                    QuizUnprocessableException.ErrorCode.FORMAT_NOT_FEASIBLE,
                    "Format " + format + " requires " + minRequired + " candidates" +
                            (needsPhoto ? " with photo" : "") +
                            " but only " + available + " eligible",
                    Map.of(
                            "requestedFormat", format.name(),
                            "minRequired", minRequired,
                            "available", available,
                            "requiresPhoto", needsPhoto));
        }

        return PlanningDecision.explicit(format);
    }

    /**
     * Choose the best format automatically based on eligibility.
     */
    private PlanningDecision planAuto(PlanningRequest request) {
        EligibilityStats stats = request.eligibility();

        if (stats.isEmpty()) {
            throw new QuizUnprocessableException(
                    QuizUnprocessableException.ErrorCode.NO_CANDIDATE,
                    "No candidates available for current constraints");
        }

        // Priority order for AUTO selection:
        // 1. MCQ if enough candidates (engaging, good for learning)
        // 2. BINARY_SWIPE if has photo (visual, quick)
        // 3. TEXT_INPUT (always feasible with 1+ candidates)

        // Try MCQ first (needs 4 candidates)
        if (stats.canMcq(4)) {
            return PlanningDecision.auto(QuizFormat.MCQ, QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT);
        }

        // Try BINARY_SWIPE if has photo
        if (stats.canPhotoFormat()) {
            return PlanningDecision.auto(QuizFormat.BINARY_SWIPE, QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT);
        }

        // Try ASSOCIATION if enough (6+ candidates)
        if (stats.canAssociation(6)) {
            return PlanningDecision.auto(QuizFormat.ASSOCIATION, QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT);
        }

        // Try ORDERING if enough (4+ candidates)
        if (stats.canOrdering(4)) {
            return PlanningDecision.auto(QuizFormat.ORDERING, QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT);
        }

        // Fallback to TEXT_INPUT (always feasible with 1+ candidates)
        if (stats.canSingleCandidate()) {
            return PlanningDecision.auto(QuizFormat.TEXT_INPUT, QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT);
        }

        // Should not reach here if stats is not empty
        throw new QuizUnprocessableException(
                QuizUnprocessableException.ErrorCode.CONSTRAINTS_IMPOSSIBLE,
                "No format feasible for current constraints");
    }

    /**
     * Check if a format is feasible given the eligibility stats.
     */
    public boolean isFeasible(QuizFormat format, EligibilityStats stats) {
        return switch (format) {
            case TEXT_INPUT, CLOZE, HANGMAN, WORD_PUZZLE -> stats.canSingleCandidate();
            case MCQ -> stats.canMcq(4);
            case BINARY_SWIPE -> stats.canPhotoFormat();
            case ASSOCIATION -> stats.canAssociation(6);
            case ORDERING -> stats.canOrdering(4);
        };
    }
}
