package com.saymyname.service.quiz.planning;

import org.springframework.stereotype.Service;

import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.quiz.planning.LearnerState;
import com.saymyname.core.model.quiz.planning.LearningObjective;
import com.saymyname.core.model.quiz.planning.PlanningContext;
import com.saymyname.core.model.quiz.planning.PlanningRequest;

/**
 * Selects the learning objective based on learner state and context constraints.
 * Objectives that require features not enabled in context are skipped.
 *
 * Business priority order:
 * 1. FATIGUE INTERVENTION (dropout prevention)
 * 2. SRS URGENT (only if context.srsEnabled)
 * 3. FLOW STATE CHALLENGE (optimize engagement)
 * 4. VARIETY BREAK (anti-monotony)
 * 5. KNOWLEDGE STATUS BASED (default path)
 */
@Service
public class ObjectiveSelector {

    // Thresholds
    private static final int FATIGUE_ERROR_STREAK_THRESHOLD = 3;
    private static final int FLOW_CORRECT_STREAK_THRESHOLD = 5;
    private static final int VARIETY_FORMAT_STREAK_THRESHOLD = 3;

    /**
     * Select learning objective based on learner state and context constraints.
     */
    public LearningObjective select(LearnerState state, PlanningRequest request) {
        PlanningContext context = request.context();

        // PRIORITY 1: FATIGUE INTERVENTION (dropout prevention)
        if (state.sessionErrorStreak() >= FATIGUE_ERROR_STREAK_THRESHOLD || state.showsFatigueSigns()) {
            return LearningObjective.CONFIDENCE_BOOST;
        }

        // PRIORITY 2: SRS URGENT (only if context.srsEnabled)
        if (context.srsEnabled() && state.srsOverdue()) {
            return LearningObjective.SRS_OVERDUE_URGENT;
        }

        // PRIORITY 3: FLOW STATE CHALLENGE (optimize engagement)
        if (state.inFlowState() && state.sessionCorrectStreak() >= FLOW_CORRECT_STREAK_THRESHOLD) {
            LearningObjective candidate = LearningObjective.CHALLENGE_READY;
            if (candidate.isValidFor(context)) {
                return candidate;
            }
            return LearningObjective.SPEED_DRILL;
        }

        // PRIORITY 4: VARIETY BREAK (anti-monotony)
        if (context.sessionTracking() && state.formatStreak() >= VARIETY_FORMAT_STREAK_THRESHOLD) {
            return LearningObjective.VARIETY_BREAK;
        }

        // PRIORITY 5: KNOWLEDGE STATUS BASED (default path)
        LearningObjective statusBased = selectByKnowledgeStatus(state, context);

        if (!statusBased.isValidFor(context)) {
            statusBased = findFallbackObjective(statusBased, context);
        }

        return statusBased;
    }

    /**
     * Select objective based on knowledge status.
     */
    private LearningObjective selectByKnowledgeStatus(LearnerState state, PlanningContext context) {
        KnowledgeStatus status = state.knowledgeStatus();

        if (status == null) {
            return LearningObjective.INTRODUCE_NEW_EASY;
        }

        return switch (status) {
            case UNKNOWN, DISCOVERED -> LearningObjective.INTRODUCE_NEW_EASY;
            case LEARNED -> state.sessionErrorStreak() > 0
                    ? LearningObjective.REINFORCE_STRUGGLING
                    : LearningObjective.PRACTICE_LEARNED;
            case MASTERED -> {
                if (context.srsEnabled() && state.srsOverdue()) {
                    yield LearningObjective.SRS_REVIEW_DUE;
                }
                if (!context.multiTargetAllowed() || state.showsFatigueSigns()) {
                    yield LearningObjective.PRACTICE_LEARNED;
                }
                yield LearningObjective.PRACTICE_MASTERED;
            }
        };
    }

    /**
     * Find a fallback objective when the preferred one isn't valid for context.
     * E.g., PRACTICE_MASTERED requires multi-target but context doesn't allow it.
     */
    private LearningObjective findFallbackObjective(LearningObjective invalid, PlanningContext context) {
        return switch (invalid) {
            case PRACTICE_MASTERED -> LearningObjective.PRACTICE_LEARNED;
            case CHALLENGE_READY -> LearningObjective.SPEED_DRILL;
            case SRS_REVIEW_DUE, SRS_OVERDUE_URGENT -> LearningObjective.PRACTICE_LEARNED;
            default -> LearningObjective.INTRODUCE_NEW_EASY;
        };
    }
}
