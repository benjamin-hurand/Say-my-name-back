package com.saymyname.core.model.quiz.planning;

/**
 * Maps a learning objective to a numeric difficulty target and timing configuration.
 * This is the bridge between the "why" (objective) and the "how" (format + candidates).
 */
public record DifficultyProfile(
        double targetDifficulty,   // 0.0 = very easy, 1.0 = very hard
        String rationale,          // For logging/analytics
        boolean allowTimedMode,    // Can this objective use timing?
        int minResponseTimeMs      // Minimum time user should need (0 = no timing)
) {

    /**
     * Create a difficulty profile for the given objective, respecting context constraints.
     */
    public static DifficultyProfile forObjective(LearningObjective objective, PlanningContext context) {
        double baseDifficulty = objective.baseDifficulty();

        // Apply context constraints if present
        if (context.difficultyRange() != null) {
            baseDifficulty = context.difficultyRange().clamp(baseDifficulty);
        }

        return switch (objective) {
            case INTRODUCE_NEW_EASY -> new DifficultyProfile(baseDifficulty, "gentle_intro", false, 0);
            case INTRODUCE_NEW_MEDIUM -> new DifficultyProfile(baseDifficulty, "intro_moderate", false, 0);
            case REINFORCE_STRUGGLING -> new DifficultyProfile(baseDifficulty, "reinforce_easy", false, 0);
            case PRACTICE_LEARNED -> new DifficultyProfile(baseDifficulty, "learned_practice", true, 6000);
            case PRACTICE_MASTERED -> new DifficultyProfile(baseDifficulty, "mastered_practice", true, 5000);
            case CHALLENGE_READY -> new DifficultyProfile(baseDifficulty, "challenge_flow", true, 4000);
            case SPEED_DRILL -> new DifficultyProfile(baseDifficulty, "speed_drill", true, 3500);
            case CONFIDENCE_BOOST -> new DifficultyProfile(baseDifficulty, "confidence_boost", false, 0);
            case VARIETY_BREAK -> new DifficultyProfile(baseDifficulty, "variety_break", false, 0);
            case SRS_REVIEW_DUE -> new DifficultyProfile(baseDifficulty, "srs_review", true, 6000);
            case SRS_OVERDUE_URGENT -> new DifficultyProfile(baseDifficulty, "srs_overdue", false, 0);
        };
    }

    /**
     * Check if timing should be enabled based on this profile and user preferences.
     */
    public boolean shouldEnableTiming(Boolean userRequestedTimed) {
        if (userRequestedTimed != null) {
            // User preference takes precedence, but only if timing is allowed
            return userRequestedTimed && allowTimedMode;
        }
        // Default: use timing if allowed and min response time is set
        return allowTimedMode && minResponseTimeMs > 0;
    }

    /**
     * Get the time limit in milliseconds, considering user preferences.
     */
    public int getTimeLimitMs(Integer userRequestedTimeLimit) {
        if (userRequestedTimeLimit != null && userRequestedTimeLimit > 0) {
            return userRequestedTimeLimit;
        }
        // Default: use min response time or a reasonable default
        return minResponseTimeMs > 0 ? minResponseTimeMs : 8000;
    }
}
