package com.saymyname.service.quiz.planning;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.saymyname.core.model.course.CourseRecentStats;
import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.course.KnowledgeStats;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.quiz.planning.LearnerState;
import com.saymyname.core.model.quiz.planning.PlanningContext;
import com.saymyname.core.model.quiz.planning.PlanningRequest;
import com.saymyname.core.model.quiz.planning.SessionStats;
import com.saymyname.service.course.CourseRecentStatsService;
import com.saymyname.service.course.KnowledgeStatsService;

/**
 * Assesses learner state by aggregating signals from various data sources.
 * Produces a LearnerState that the ObjectiveSelector uses for decision making.
 */
@Service
public class LearnerStateAssessor {

    // Thresholds (from existing policies)
    private static final int STRESS_ERROR_STREAK = 2;
    private static final double STRESS_HELP_RECENT = 1.0;
    private static final double SLOW_AVG_RT_MS = 9000;
    private static final int MIN_FLUENT_ATTEMPTS = 3;
    private static final int MAX_ERROR_STREAK_FOR_FLUENCY = 0;
    private static final double MAX_HELP_RECENT_FOR_FLUENCY = 0.5;
    private static final double MAX_AVG_RT_FOR_FLUENCY = 8000;

    private final KnowledgeStatsService knowledgeStatsService;
    private final CourseRecentStatsService courseRecentStatsService;

    public LearnerStateAssessor(
            KnowledgeStatsService knowledgeStatsService,
            CourseRecentStatsService courseRecentStatsService) {
        this.knowledgeStatsService = knowledgeStatsService;
        this.courseRecentStatsService = courseRecentStatsService;
    }

    /**
     * Assess learner state from the planning request.
     */
    public LearnerState assess(PlanningRequest request) {
        LearnerState.Builder builder = LearnerState.builder();

        // 1. Apply session stats if provided
        if (request.sessionStats() != null) {
            builder.fromSessionStats(request.sessionStats());
        }

        // 2. Load course-level stats if course context
        PlanningContext context = request.context();
        if (context.isCourse() && context.courseId() != null) {
            applyCourseStats(builder, context.courseId());
        }

        // 3. Load knowledge-specific stats if knowledge is provided
        Knowledge knowledge = request.primaryKnowledge();
        if (knowledge != null) {
            applyKnowledgeData(builder, knowledge);
            applyKnowledgeStats(builder, request, knowledge);
        }

        // 4. Compute derived signals
        LearnerState partial = builder.build();
        boolean showsFatigue = detectFatigue(partial);
        boolean inFlow = detectFlowState(partial);

        return builder
                .showsFatigueSigns(showsFatigue)
                .inFlowState(inFlow)
                .build();
    }

    /**
     * Apply course-level stats (format streak, error streak, etc.)
     */
    private void applyCourseStats(LearnerState.Builder builder, Long courseId) {
        CourseRecentStats courseStats = courseRecentStatsService.getStatsForCourse(courseId);
        if (courseStats == null) {
            return;
        }

        builder.formatStreak(courseStats.getFormatStreak())
                .lastFormat(courseStats.getLastFormat())
                .sessionErrorStreak(courseStats.getErrorStreak())
                .avgResponseTimeMs(courseStats.getAvgRtRecent());
    }

    /**
     * Apply data directly from Knowledge entity.
     */
    private void applyKnowledgeData(LearnerState.Builder builder, Knowledge knowledge) {
        builder.knowledgeStatus(knowledge.getStatus())
                .knowledgeAttempts(knowledge.getTotalRepetitionCount())
                .knowledgeSrsStreak(knowledge.getSrsStreak());

        // Check if SRS overdue
        LocalDateTime nextReview = knowledge.getNextReviewDate();
        boolean srsOverdue = nextReview != null && nextReview.isBefore(LocalDateTime.now());
        builder.srsOverdue(srsOverdue);
    }

    /**
     * Apply performance stats from KnowledgeStats.
     */
    private void applyKnowledgeStats(
            LearnerState.Builder builder,
            PlanningRequest request,
            Knowledge knowledge) {
        if (request.userId() == null || request.gameModeId() == null || knowledge.getId() == null) {
            return;
        }

        Map<Long, KnowledgeStats> statsMap = knowledgeStatsService.getStatsForKnowledgeIds(
                request.userId(),
                request.gameModeId(),
                List.of(knowledge.getId()));

        KnowledgeStats stats = statsMap.get(knowledge.getId());
        if (stats == null) {
            return;
        }

        // Compute recent accuracy
        double recentAccuracy = 0.5;
        if (stats.getAttemptsRecent() > 0) {
            recentAccuracy = stats.getCorrectRecent() / stats.getAttemptsRecent();
        }

        builder.recentAccuracy(recentAccuracy)
                .avgResponseTimeMs(stats.getAvgRtRecent())
                .helpUsageRecent(stats.getHelpRecent())
                .sessionErrorStreak(stats.getErrorStreak());
    }

    /**
     * Detect fatigue signs based on thresholds.
     * Fatigue = errorStreak >= 2 OR helpRecent > 1.0 OR avgRt > 9000ms
     */
    private boolean detectFatigue(LearnerState state) {
        return state.sessionErrorStreak() >= STRESS_ERROR_STREAK
                || state.helpUsageRecent() > STRESS_HELP_RECENT
                || state.avgResponseTimeMs() > SLOW_AVG_RT_MS;
    }

    /**
     * Detect flow state based on thresholds.
     * Flow = attempts >= 3 AND errorStreak = 0 AND helpRecent <= 0.5 AND 0 < avgRt <= 8000ms
     */
    private boolean detectFlowState(LearnerState state) {
        if (state.knowledgeAttempts() < MIN_FLUENT_ATTEMPTS) {
            return false;
        }
        if (state.sessionErrorStreak() > MAX_ERROR_STREAK_FOR_FLUENCY) {
            return false;
        }
        if (state.helpUsageRecent() > MAX_HELP_RECENT_FOR_FLUENCY) {
            return false;
        }
        double avgRt = state.avgResponseTimeMs();
        return avgRt > 0 && avgRt <= MAX_AVG_RT_FOR_FLUENCY;
    }
}
