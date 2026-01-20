package com.saymyname.core.model.quiz.planning;

import java.time.Instant;

import com.saymyname.core.model.enums.quiz.QuizFormat;

/**
 * Metrics logged for every question planned.
 * Enables A/B testing and optimization.
 */
public record QuestionPlanMetrics(
        Long userId,
        Long gameModeId,
        String contextType,                    // "course", "training", "challenge", etc.

        // Decision context
        LearningObjective objective,
        double targetDifficulty,
        QuizFormat selectedFormat,

        // Learner state at decision time
        int sessionAnswered,
        int sessionCorrectStreak,
        double recentAccuracy,
        boolean showedFatigueSigns,
        boolean wasInFlowState,

        // Distractor selection
        QuestionPlan.DistractorStrategy distractorSelectionStrategy,
        double avgDistractorSimilarity,

        // Timing
        Instant emittedAt
) {

    /**
     * Builder for fluent construction.
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long userId;
        private Long gameModeId;
        private String contextType;
        private LearningObjective objective;
        private double targetDifficulty;
        private QuizFormat selectedFormat;
        private int sessionAnswered;
        private int sessionCorrectStreak;
        private double recentAccuracy;
        private boolean showedFatigueSigns;
        private boolean wasInFlowState;
        private QuestionPlan.DistractorStrategy distractorSelectionStrategy;
        private double avgDistractorSimilarity;
        private Instant emittedAt;

        public Builder userId(Long val) { this.userId = val; return this; }
        public Builder gameModeId(Long val) { this.gameModeId = val; return this; }
        public Builder contextType(String val) { this.contextType = val; return this; }
        public Builder objective(LearningObjective val) { this.objective = val; return this; }
        public Builder targetDifficulty(double val) { this.targetDifficulty = val; return this; }
        public Builder selectedFormat(QuizFormat val) { this.selectedFormat = val; return this; }
        public Builder sessionAnswered(int val) { this.sessionAnswered = val; return this; }
        public Builder sessionCorrectStreak(int val) { this.sessionCorrectStreak = val; return this; }
        public Builder recentAccuracy(double val) { this.recentAccuracy = val; return this; }
        public Builder showedFatigueSigns(boolean val) { this.showedFatigueSigns = val; return this; }
        public Builder wasInFlowState(boolean val) { this.wasInFlowState = val; return this; }
        public Builder distractorSelectionStrategy(QuestionPlan.DistractorStrategy val) {
            this.distractorSelectionStrategy = val; return this;
        }
        public Builder avgDistractorSimilarity(double val) { this.avgDistractorSimilarity = val; return this; }
        public Builder emittedAt(Instant val) { this.emittedAt = val; return this; }

        public QuestionPlanMetrics build() {
            return new QuestionPlanMetrics(
                    userId, gameModeId, contextType, objective, targetDifficulty,
                    selectedFormat, sessionAnswered, sessionCorrectStreak, recentAccuracy,
                    showedFatigueSigns, wasInFlowState, distractorSelectionStrategy,
                    avgDistractorSimilarity, emittedAt
            );
        }
    }
}
