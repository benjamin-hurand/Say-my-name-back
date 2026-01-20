package com.saymyname.core.model.quiz.planning;

import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.enums.FollowFilter;
import com.saymyname.core.model.enums.PoolType;
import com.saymyname.core.model.enums.quiz.QuizPreferredFormat;
import com.saymyname.core.model.quiz.options.GameOptions;

/**
 * Input to QuestionPlanningService.
 * Contains all information needed to plan a question.
 */
public record PlanningRequest(
        // Identity
        Long userId,
        Long gameModeId,
        FollowFilter populationScope,             // FOLLOWED, ALL

        // Game options (for candidate selection)
        GameOptions gameOptions,

        // Context configuration (replaces QuizQuestionSource)
        PlanningContext context,

        // Course context (optional, only for course planning)
        Course course,

        // Pre-selected knowledge (optional, depends on context)
        Knowledge primaryKnowledge,        // null for random selection
        PoolType selectedPoolType,         // null if not SRS-based

        // Session stats (optional, only if context.sessionTracking=true)
        SessionStats sessionStats,         // null if no session tracking

        // User preferences
        QuizPreferredFormat preferredFormat,
        Boolean requestedTimed,
        Integer requestedTimeLimitMs,

        // Anti-repeat
        Long lastPersonId,
        Boolean lastCorrect
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
        private FollowFilter populationScope;
        private GameOptions gameOptions;
        private PlanningContext context;
        private Course course;
        private Knowledge primaryKnowledge;
        private PoolType selectedPoolType;
        private SessionStats sessionStats;
        private QuizPreferredFormat preferredFormat;
        private Boolean requestedTimed;
        private Integer requestedTimeLimitMs;
        private Long lastPersonId;
        private Boolean lastCorrect;

        public Builder userId(Long val) { this.userId = val; return this; }
        public Builder gameModeId(Long val) { this.gameModeId = val; return this; }
        public Builder populationScope(FollowFilter val) { this.populationScope = val; return this; }
        public Builder gameOptions(GameOptions val) { this.gameOptions = val; return this; }
        public Builder context(PlanningContext val) { this.context = val; return this; }
        public Builder course(Course val) { this.course = val; return this; }
        public Builder primaryKnowledge(Knowledge val) { this.primaryKnowledge = val; return this; }
        public Builder selectedPoolType(PoolType val) { this.selectedPoolType = val; return this; }
        public Builder sessionStats(SessionStats val) { this.sessionStats = val; return this; }
        public Builder preferredFormat(QuizPreferredFormat val) { this.preferredFormat = val; return this; }
        public Builder requestedTimed(Boolean val) { this.requestedTimed = val; return this; }
        public Builder requestedTimeLimitMs(Integer val) { this.requestedTimeLimitMs = val; return this; }
        public Builder lastPersonId(Long val) { this.lastPersonId = val; return this; }
        public Builder lastCorrect(Boolean val) { this.lastCorrect = val; return this; }

        public PlanningRequest build() {
            return new PlanningRequest(
                    userId, gameModeId, populationScope, gameOptions, context,
                    course, primaryKnowledge, selectedPoolType, sessionStats, preferredFormat,
                    requestedTimed, requestedTimeLimitMs, lastPersonId, lastCorrect
            );
        }
    }
}
