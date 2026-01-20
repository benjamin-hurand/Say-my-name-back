package com.saymyname.core.model.quiz.planning;

import java.util.Map;
import java.util.Set;

import com.saymyname.core.model.enums.quiz.QuizFormat;

/**
 * Universal context configuration for question planning.
 * Parameterized to work for any learning context (course, training, challenge, minigame).
 *
 * Use factory methods to create standard contexts:
 * - {@link #forCourse(Long)} - Course mode with SRS, knowledge tracking, multi-target
 * - {@link #forTraining(boolean)} - Training mode with optional knowledge tracking
 * - {@link #forChallenge(Long, DifficultyRange)} - Challenge mode with fixed difficulty
 * - {@link #forMinigame(String, Set)} - Minigame mode with specific formats
 */
public record PlanningContext(
        // Identity
        String contextType,              // "course", "training", "challenge", "minigame"

        // Feature flags
        boolean srsEnabled,              // Should we use SRS pools for candidate selection?
        boolean knowledgeTracking,       // Should answers update Knowledge entities?
        boolean multiTargetAllowed,      // Can use ORDERING/ASSOCIATION formats?
        boolean sessionTracking,         // Track session-level metrics (streak, etc.)?

        // Format constraints
        Set<QuizFormat> allowedFormats,  // null = all formats allowed
        DifficultyRange difficultyRange, // e.g., [0.2, 0.8] or null for no constraint

        // Optional context-specific identifiers
        Long courseId,                   // if contextType = "course"
        Long challengeId,                // if contextType = "challenge"
        Map<String, Object> metadata     // extensibility for future contexts
) {

    /**
     * Course mode: Full SRS, knowledge tracking, multi-target, session tracking.
     */
    public static PlanningContext forCourse(Long courseId) {
        return new PlanningContext(
                "course",
                true,   // SRS enabled
                true,   // Knowledge tracking
                true,   // Multi-target allowed
                true,   // Session tracking
                null,   // All formats
                null,   // No difficulty constraint
                courseId,
                null,
                Map.of()
        );
    }

    /**
     * Training mode with configurable knowledge tracking.
     * No SRS, no multi-target, no session tracking.
     *
     * @param trackKnowledge if true, answers update Knowledge stats (SRS progression)
     */
    public static PlanningContext forTraining(boolean trackKnowledge) {
        return new PlanningContext(
                "training",
                false,          // No SRS (random selection)
                trackKnowledge, // Knowledge tracking = user choice via GameOptions
                false,          // No multi-target
                false,          // No session tracking
                null,           // All formats
                null,           // No difficulty constraint
                null,
                null,
                Map.of()
        );
    }

    /**
     * Training mode with knowledge tracking enabled (default behavior).
     */
    public static PlanningContext forTraining() {
        return forTraining(true);
    }

    /**
     * Challenge mode: Fixed difficulty range, fast formats only, no SRS pollution.
     */
    public static PlanningContext forChallenge(Long challengeId, DifficultyRange range) {
        return new PlanningContext(
                "challenge",
                false,  // No SRS (challenges are time-boxed)
                false,  // No knowledge tracking (avoid SRS pollution)
                true,   // Multi-target allowed
                true,   // Session tracking (for challenge progress)
                Set.of(QuizFormat.MCQ, QuizFormat.TEXT_INPUT),
                range,
                null,
                challengeId,
                Map.of()
        );
    }

    /**
     * Minigame mode: Specific formats, easy difficulty, no SRS/knowledge tracking.
     */
    public static PlanningContext forMinigame(String minigameType, Set<QuizFormat> formats) {
        return new PlanningContext(
                "minigame",
                false,  // No SRS
                false,  // No knowledge tracking
                false,  // No multi-target
                true,   // Session tracking (for minigame progress)
                formats,
                DifficultyRange.easy(),
                null,
                null,
                Map.of("minigameType", minigameType)
        );
    }

    /**
     * Check if a format is allowed in this context.
     */
    public boolean isFormatAllowed(QuizFormat format) {
        return allowedFormats == null || allowedFormats.isEmpty() || allowedFormats.contains(format);
    }

    /**
     * Check if this is a course context.
     */
    public boolean isCourse() {
        return "course".equals(contextType);
    }

    /**
     * Check if this is a training context.
     */
    public boolean isTraining() {
        return "training".equals(contextType);
    }

    /**
     * Check if this is a challenge context.
     */
    public boolean isChallenge() {
        return "challenge".equals(contextType);
    }

    /**
     * Check if this is a minigame context.
     */
    public boolean isMinigame() {
        return "minigame".equals(contextType);
    }
}
