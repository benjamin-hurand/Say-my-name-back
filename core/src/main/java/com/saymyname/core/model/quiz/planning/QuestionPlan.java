package com.saymyname.core.model.quiz.planning;

import java.util.List;

import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.enums.PoolType;
import com.saymyname.core.model.enums.quiz.QuizDecisionReasonCode;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.people.Person;

/**
 * Output from QuestionPlanningService.
 * Contains all decisions needed to build a quiz question.
 */
public record QuestionPlan(
        // Learning objective (the "why")
        LearningObjective objective,
        DifficultyProfile difficultyProfile,

        // Format + candidates (coupled selection result)
        QuizFormat format,
        Person primaryCandidate,
        List<Person> selectedDistractors,
        List<Long> candidatePoolIds,

        // Multi-target (ORDERING/ASSOCIATION) - only if context.multiTargetAllowed
        List<Knowledge> targetKnowledges,
        int targetCount,

        // Timing configuration
        Boolean timed,
        Integer timeLimitMs,

        // Metadata for analytics
        QuizDecisionReasonCode reasonCode,
        String difficultyRationale,
        String reasonDetailsJson,
        DistractorStrategy distractorStrategy,
        PoolType selectedPoolType
) {

    /**
     * Distractor selection strategy used.
     */
    public enum DistractorStrategy {
        DISTINCT,   // Maximize Levenshtein distance (easy)
        MIXED,      // 1 similar + N-1 distinct (medium)
        SIMILAR     // Minimize Levenshtein distance (hard)
    }

    /**
     * Builder for fluent construction.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Check if this plan uses multi-target formats.
     */
    public boolean isMultiTarget() {
        return format == QuizFormat.ORDERING || format == QuizFormat.ASSOCIATION;
    }

    /**
     * Check if timing is enabled.
     */
    public boolean isTimedMode() {
        return timed != null && timed;
    }

    public static class Builder {
        private LearningObjective objective;
        private DifficultyProfile difficultyProfile;
        private QuizFormat format;
        private Person primaryCandidate;
        private List<Person> selectedDistractors;
        private List<Long> candidatePoolIds;
        private List<Knowledge> targetKnowledges;
        private int targetCount = 1;
        private Boolean timed;
        private Integer timeLimitMs;
        private QuizDecisionReasonCode reasonCode;
        private String difficultyRationale;
        private String reasonDetailsJson;
        private DistractorStrategy distractorStrategy;
        private PoolType selectedPoolType;

        public Builder objective(LearningObjective val) { this.objective = val; return this; }
        public Builder difficultyProfile(DifficultyProfile val) { this.difficultyProfile = val; return this; }
        public Builder format(QuizFormat val) { this.format = val; return this; }
        public Builder primaryCandidate(Person val) { this.primaryCandidate = val; return this; }
        public Builder selectedDistractors(List<Person> val) { this.selectedDistractors = val; return this; }
        public Builder candidatePoolIds(List<Long> val) { this.candidatePoolIds = val; return this; }
        public Builder targetKnowledges(List<Knowledge> val) { this.targetKnowledges = val; return this; }
        public Builder targetCount(int val) { this.targetCount = val; return this; }
        public Builder timed(Boolean val) { this.timed = val; return this; }
        public Builder timeLimitMs(Integer val) { this.timeLimitMs = val; return this; }
        public Builder reasonCode(QuizDecisionReasonCode val) { this.reasonCode = val; return this; }
        public Builder difficultyRationale(String val) { this.difficultyRationale = val; return this; }
        public Builder reasonDetailsJson(String val) { this.reasonDetailsJson = val; return this; }
        public Builder distractorStrategy(DistractorStrategy val) { this.distractorStrategy = val; return this; }
        public Builder selectedPoolType(PoolType val) { this.selectedPoolType = val; return this; }

        public QuestionPlan build() {
            return new QuestionPlan(
                    objective, difficultyProfile, format, primaryCandidate,
                    selectedDistractors, candidatePoolIds, targetKnowledges, targetCount,
                    timed, timeLimitMs, reasonCode, difficultyRationale,
                    reasonDetailsJson, distractorStrategy, selectedPoolType
            );
        }
    }
}
