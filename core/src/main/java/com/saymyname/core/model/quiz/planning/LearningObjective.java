package com.saymyname.core.model.quiz.planning;

/**
 * Learning objectives that drive question planning decisions.
 * Each objective has associated requirements and a base difficulty target.
 *
 * The planning service selects an objective based on learner state,
 * then maps it to a difficulty profile which drives format + candidate selection.
 */
public enum LearningObjective {

    // ACQUISITION: New content introduction
    INTRODUCE_NEW_EASY(false, false, 0.20),
    INTRODUCE_NEW_MEDIUM(false, false, 0.40),

    // CONSOLIDATION: Strengthen existing knowledge
    REINFORCE_STRUGGLING(false, false, 0.25),
    PRACTICE_LEARNED(false, false, 0.50),
    PRACTICE_MASTERED(true, false, 0.60),    // Can use multi-target formats

    // CHALLENGE: Push user into growth zone
    CHALLENGE_READY(true, false, 0.70),      // Can use multi-target formats
    SPEED_DRILL(false, false, 0.65),

    // RE-ENGAGEMENT: Prevent frustration/dropout
    CONFIDENCE_BOOST(false, false, 0.15),
    VARIETY_BREAK(false, false, 0.40),

    // SRS: Spaced repetition priorities
    SRS_REVIEW_DUE(false, true, 0.50),       // Requires SRS enabled
    SRS_OVERDUE_URGENT(false, true, 0.35);   // Requires SRS enabled

    private final boolean requiresMultiTarget;
    private final boolean requiresSrs;
    private final double baseDifficulty;

    LearningObjective(boolean requiresMultiTarget, boolean requiresSrs, double baseDifficulty) {
        this.requiresMultiTarget = requiresMultiTarget;
        this.requiresSrs = requiresSrs;
        this.baseDifficulty = baseDifficulty;
    }

    /**
     * Whether this objective requires multi-target formats (ORDERING, ASSOCIATION).
     */
    public boolean requiresMultiTarget() {
        return requiresMultiTarget;
    }

    /**
     * Whether this objective requires SRS (spaced repetition) to be enabled.
     */
    public boolean requiresSrs() {
        return requiresSrs;
    }

    /**
     * Base difficulty target for this objective (0.0-1.0).
     * May be adjusted by context constraints.
     */
    public double baseDifficulty() {
        return baseDifficulty;
    }

    /**
     * Check if this objective is valid for the given planning context.
     * An objective is invalid if it requires features not enabled in context.
     */
    public boolean isValidFor(PlanningContext context) {
        if (requiresSrs && !context.srsEnabled()) {
            return false;
        }
        if (requiresMultiTarget && !context.multiTargetAllowed()) {
            return false;
        }
        return true;
    }
}
