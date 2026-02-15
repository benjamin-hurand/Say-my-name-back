// service/quiz/planning/FormatPlanner.java
package com.saymyname.service.quiz.planning;

import org.springframework.stereotype.Service;

import com.saymyname.core.exception.quiz.QuizUnprocessableException;
import com.saymyname.core.model.enums.KnowledgeStatus;
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
     * Choose the best format automatically based on eligibility and knowledge status.
     *
     * D1 Ladder (when knowledgeStatus is available):
     * - UNKNOWN/DISCOVERED → recognition formats (MCQ, BINARY_SWIPE)
     * - LEARNED → recall formats (TEXT_INPUT, CLOZE)
     * - MASTERED → transfer formats (ASSOCIATION, ORDERING)
     */
    private PlanningDecision planAuto(PlanningRequest request) {
        EligibilityStats stats = request.eligibility();
        KnowledgeStatus status = request.knowledgeStatus();

        if (stats.isEmpty()) {
            throw new QuizUnprocessableException(
                    QuizUnprocessableException.ErrorCode.NO_CANDIDATE,
                    "No candidates available for current constraints");
        }

        // D1: Ladder-based selection when knowledgeStatus is available (COURSE mode)
        if (status != null) {
            return planWithLadder(stats, status);
        }

        // TRAINING mode: eligibility-only selection (existing behavior)
        return planByEligibilityOnly(stats);
    }

    /**
     * D1: Ladder-based format selection.
     * Selects format based on knowledge mastery level:
     * - Recognition (easy): MCQ, BINARY_SWIPE - for UNKNOWN/DISCOVERED
     * - Recall (medium): TEXT_INPUT, CLOZE - for LEARNED
     * - Transfer (hard): ASSOCIATION, ORDERING - for MASTERED
     */
    private PlanningDecision planWithLadder(EligibilityStats stats, KnowledgeStatus status) {
        return switch (status) {
            case UNKNOWN, DISCOVERED -> planRecognition(stats);
            case LEARNED -> planRecall(stats);
            case MASTERED -> planTransfer(stats);
        };
    }

    /**
     * Recognition tier: easy formats for UNKNOWN/DISCOVERED status.
     * Priority: MCQ > BINARY_SWIPE > fallback to recall
     */
    private PlanningDecision planRecognition(EligibilityStats stats) {
        if (stats.canMcq(4)) {
            return PlanningDecision.auto(QuizFormat.MCQ, QuizDecisionReasonCode.LADDER_RECOGNITION_MCQ);
        }
        if (stats.canPhotoFormat()) {
            return PlanningDecision.auto(QuizFormat.BINARY_SWIPE, QuizDecisionReasonCode.LADDER_RECOGNITION_SWIPE);
        }
        // Fallback to recall formats if recognition not feasible
        return planRecall(stats);
    }

    /**
     * Recall tier: medium formats for LEARNED status.
     * Priority: TEXT_INPUT > CLOZE > fallback to recognition
     */
    private PlanningDecision planRecall(EligibilityStats stats) {
        if (stats.canSingleCandidate()) {
            return PlanningDecision.auto(QuizFormat.TEXT_INPUT, QuizDecisionReasonCode.LADDER_RECALL_TEXT);
        }
        // If TEXT_INPUT not feasible, this shouldn't happen with canSingleCandidate
        // but fallback to recognition as safety
        return planRecognition(stats);
    }

    /**
     * Transfer tier: hard formats for MASTERED status.
     * Priority: ASSOCIATION > ORDERING > fallback to recall
     */
    private PlanningDecision planTransfer(EligibilityStats stats) {
        if (stats.canAssociation(6)) {
            return PlanningDecision.auto(QuizFormat.ASSOCIATION, QuizDecisionReasonCode.LADDER_TRANSFER_ASSOCIATION);
        }
        if (stats.canOrdering(4)) {
            return PlanningDecision.auto(QuizFormat.ORDERING, QuizDecisionReasonCode.LADDER_TRANSFER_ORDERING);
        }
        // Fallback to recall if transfer formats not feasible
        return planRecall(stats);
    }

    /**
     * TRAINING mode: eligibility-only selection (original behavior).
     * Priority: MCQ > BINARY_SWIPE > ASSOCIATION > ORDERING > TEXT_INPUT
     */
    private PlanningDecision planByEligibilityOnly(EligibilityStats stats) {
        if (stats.canMcq(4)) {
            return PlanningDecision.auto(QuizFormat.MCQ, QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT);
        }
        if (stats.canPhotoFormat()) {
            return PlanningDecision.auto(QuizFormat.BINARY_SWIPE, QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT);
        }
        if (stats.canAssociation(6)) {
            return PlanningDecision.auto(QuizFormat.ASSOCIATION, QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT);
        }
        if (stats.canOrdering(4)) {
            return PlanningDecision.auto(QuizFormat.ORDERING, QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT);
        }
        if (stats.canSingleCandidate()) {
            return PlanningDecision.auto(QuizFormat.TEXT_INPUT, QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT);
        }

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
