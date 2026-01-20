package com.saymyname.core.model.quiz.planning;

import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.enums.quiz.QuizFormat;

/**
 * Aggregated learner signals for objective selection.
 * Combines session metrics, recent performance, and knowledge-specific data.
 *
 * All fields are null-safe with sensible defaults for missing data.
 */
public record LearnerState(
        // Session metrics (from SessionStats, or defaults if not tracked)
        int sessionAnswered,
        int sessionCorrectStreak,
        int sessionErrorStreak,
        double sessionAccuracy,
        long sessionDurationMs,

        // Recent performance (from KnowledgeStats, or defaults)
        double recentAccuracy,
        double avgResponseTimeMs,
        double helpUsageRecent,

        // Knowledge-specific (null-safe)
        KnowledgeStatus knowledgeStatus,  // null if no Knowledge
        int knowledgeAttempts,
        int knowledgeSrsStreak,
        boolean srsOverdue,

        // Fatigue/engagement signals (computed)
        boolean showsFatigueSigns,
        boolean inFlowState,
        int formatStreak,
        QuizFormat lastFormat
) {

    /**
     * Create a LearnerState with sensible defaults for missing data.
     */
    public static LearnerState withDefaults() {
        return new LearnerState(
                0, 0, 0, 0.5, 0,           // session
                0.5, 0, 0,                 // recent
                null, 0, 0, false,         // knowledge
                false, false, 0, null      // signals
        );
    }

    /**
     * Builder for fluent construction.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Check if the learner has any knowledge context.
     */
    public boolean hasKnowledge() {
        return knowledgeStatus != null;
    }

    /**
     * Check if the learner is a beginner (no attempts or UNKNOWN status).
     */
    public boolean isBeginner() {
        return knowledgeAttempts == 0 || knowledgeStatus == KnowledgeStatus.UNKNOWN;
    }

    /**
     * Check if the learner has mastered the current content.
     */
    public boolean isMastered() {
        return knowledgeStatus == KnowledgeStatus.MASTERED;
    }

    /**
     * Check if the learner has learned but not mastered the current content.
     */
    public boolean isLearned() {
        return knowledgeStatus == KnowledgeStatus.LEARNED;
    }

    /**
     * Check if the learner is in discovery phase.
     */
    public boolean isDiscovering() {
        return knowledgeStatus == KnowledgeStatus.DISCOVERED;
    }

    public static class Builder {
        private int sessionAnswered = 0;
        private int sessionCorrectStreak = 0;
        private int sessionErrorStreak = 0;
        private double sessionAccuracy = 0.5;
        private long sessionDurationMs = 0;
        private double recentAccuracy = 0.5;
        private double avgResponseTimeMs = 0;
        private double helpUsageRecent = 0;
        private KnowledgeStatus knowledgeStatus = null;
        private int knowledgeAttempts = 0;
        private int knowledgeSrsStreak = 0;
        private boolean srsOverdue = false;
        private boolean showsFatigueSigns = false;
        private boolean inFlowState = false;
        private int formatStreak = 0;
        private QuizFormat lastFormat = null;

        public Builder sessionAnswered(int val) { this.sessionAnswered = val; return this; }
        public Builder sessionCorrectStreak(int val) { this.sessionCorrectStreak = val; return this; }
        public Builder sessionErrorStreak(int val) { this.sessionErrorStreak = val; return this; }
        public Builder sessionAccuracy(double val) { this.sessionAccuracy = val; return this; }
        public Builder sessionDurationMs(long val) { this.sessionDurationMs = val; return this; }
        public Builder recentAccuracy(double val) { this.recentAccuracy = val; return this; }
        public Builder avgResponseTimeMs(double val) { this.avgResponseTimeMs = val; return this; }
        public Builder helpUsageRecent(double val) { this.helpUsageRecent = val; return this; }
        public Builder knowledgeStatus(KnowledgeStatus val) { this.knowledgeStatus = val; return this; }
        public Builder knowledgeAttempts(int val) { this.knowledgeAttempts = val; return this; }
        public Builder knowledgeSrsStreak(int val) { this.knowledgeSrsStreak = val; return this; }
        public Builder srsOverdue(boolean val) { this.srsOverdue = val; return this; }
        public Builder showsFatigueSigns(boolean val) { this.showsFatigueSigns = val; return this; }
        public Builder inFlowState(boolean val) { this.inFlowState = val; return this; }
        public Builder formatStreak(int val) { this.formatStreak = val; return this; }
        public Builder lastFormat(QuizFormat val) { this.lastFormat = val; return this; }

        /**
         * Apply session stats to this builder.
         */
        public Builder fromSessionStats(SessionStats stats) {
            if (stats != null) {
                this.sessionAnswered = stats.answered();
                this.sessionCorrectStreak = stats.correctStreak();
                this.sessionErrorStreak = stats.errorStreak();
                this.sessionAccuracy = stats.accuracy();
                this.sessionDurationMs = stats.durationMs();
                this.formatStreak = stats.formatStreak();
                this.lastFormat = stats.lastFormat();
            }
            return this;
        }

        public LearnerState build() {
            return new LearnerState(
                    sessionAnswered, sessionCorrectStreak, sessionErrorStreak,
                    sessionAccuracy, sessionDurationMs,
                    recentAccuracy, avgResponseTimeMs, helpUsageRecent,
                    knowledgeStatus, knowledgeAttempts, knowledgeSrsStreak, srsOverdue,
                    showsFatigueSigns, inFlowState, formatStreak, lastFormat
            );
        }
    }
}
