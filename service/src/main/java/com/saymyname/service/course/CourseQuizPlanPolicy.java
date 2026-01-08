// src/main/java/com/saymyname/service/course/CourseQuizPlanPolicy.java
package com.saymyname.service.course;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.CourseQuestionHistory;
import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.enums.quiz.QuizPreferredFormat;

@Component
public class CourseQuizPlanPolicy {

    private static final int MIN_TIMED_STREAK = 2;
    private static final int FAST_RESPONSE_TIME_MS = 8000;
    private static final int DEFAULT_TIME_LIMIT_MS = 8000;
    private static final double MAX_ERROR_RATE = 0.2;
    private static final int MULTI_TARGET_MIN = 4;

    public record Plan(
            QuizFormat format,
            boolean timed,
            Integer timeLimitMs,
            int targetCount,
            String paramsJson,
            String reason) {
        public Plan {
            if (format == null) {
                throw new IllegalArgumentException("format is required");
            }
            if (targetCount < 1) {
                throw new IllegalArgumentException("targetCount must be >= 1");
            }
            if (timed && (timeLimitMs == null || timeLimitMs < 1000)) {
                throw new IllegalArgumentException("timeLimitMs must be >= 1000 when timed=true");
            }
        }
    }

    public Plan decide(
            Course course,
            CourseQuestionHistory previousHistory,
            Knowledge nextKnowledge,
            QuizPreferredFormat preferredFormatQuery,
            Boolean requestedTimed,
            Integer requestedTimeLimitMs,
            boolean multiTargetAvailable) {

        QuizPreferredFormat explicit = normalizePreferred(preferredFormatQuery);
        if (explicit != null) {
            QuizFormat fmt = QuizFormat.valueOf(explicit.name());
            return explicitPlan(fmt, requestedTimed, requestedTimeLimitMs, multiTargetAvailable);
        }

        return decideAuto(course, previousHistory, nextKnowledge, requestedTimed, requestedTimeLimitMs,
                multiTargetAvailable);
    }

    private Plan explicitPlan(
            QuizFormat format,
            Boolean requestedTimed,
            Integer requestedTimeLimitMs,
            boolean multiTargetAvailable) {
        boolean timed = requestedTimed != null && requestedTimed;
        Integer timeLimitMs = resolveTimeLimitMs(timed, requestedTimeLimitMs);
        int targetCount = (format == QuizFormat.ORDERING || format == QuizFormat.ASSOCIATION)
                ? MULTI_TARGET_MIN
                : 1;
        if (targetCount > 1 && !multiTargetAvailable) {
            return mcqPlan(timed, timeLimitMs, "EXPLICIT_MULTI_TARGET_UNAVAILABLE");
        }
        String paramsJson = (format == QuizFormat.MCQ) ? "{\"nbChoices\":4}" : null;
        return new Plan(format, timed, timeLimitMs, targetCount, paramsJson, "EXPLICIT");
    }

    private Plan decideAuto(
            Course course,
            CourseQuestionHistory previousHistory,
            Knowledge nextKnowledge,
            Boolean requestedTimed,
            Integer requestedTimeLimitMs,
            boolean multiTargetAvailable) {

        if (nextKnowledge == null) {
            throw new IllegalStateException("CourseQuizPlanPolicy requires target Knowledge");
        }

        KnowledgeStatus status = nextKnowledge.getStatus() != null
                ? nextKnowledge.getStatus()
                : KnowledgeStatus.UNKNOWN;

        if (status == KnowledgeStatus.UNKNOWN || status == KnowledgeStatus.DISCOVERED) {
            return applyTimingOverride(
                    mcqPlan(false, null, "AUTO_UNKNOWN_OR_DISCOVERED"),
                    requestedTimed,
                    requestedTimeLimitMs);
        }

        if (status == KnowledgeStatus.LEARNED) {
            boolean streakOk = nextKnowledge.getSrsStreak() >= MIN_TIMED_STREAK;
            boolean fast = isFastRecent(previousHistory);
            boolean timed = streakOk && fast;
            Integer timeLimitMs = resolveTimeLimitMs(timed, DEFAULT_TIME_LIMIT_MS);
            return applyTimingOverride(
                    new Plan(QuizFormat.TEXT_INPUT, timed, timeLimitMs, 1, null, "AUTO_LEARNED"),
                    requestedTimed,
                    requestedTimeLimitMs);
        }

        boolean lowErrorRate = hasLowErrorRate(nextKnowledge);
        if (status == KnowledgeStatus.MASTERED && multiTargetAvailable) {
            QuizFormat fmt = chooseMultiTargetFormat(previousHistory);
            return applyTimingOverride(
                    new Plan(fmt, false, null, MULTI_TARGET_MIN, null, "AUTO_MULTI_TARGET"),
                    requestedTimed,
                    requestedTimeLimitMs);
        }

        QuizFormat fmt = lowErrorRate ? QuizFormat.CLOZE : QuizFormat.TEXT_INPUT;
        boolean timed = lowErrorRate;
        Integer timeLimitMs = resolveTimeLimitMs(timed, DEFAULT_TIME_LIMIT_MS);
        return applyTimingOverride(
                new Plan(fmt, timed, timeLimitMs, 1, null, "AUTO_MASTERED"),
                requestedTimed,
                requestedTimeLimitMs);
    }

    private static QuizPreferredFormat normalizePreferred(QuizPreferredFormat preferred) {
        if (preferred == null || preferred == QuizPreferredFormat.AUTO) {
            return null;
        }
        return preferred;
    }

    private static boolean hasLowErrorRate(Knowledge knowledge) {
        int failures = Math.max(0, knowledge.getFailureCount());
        int successes = Math.max(0, knowledge.getSuccessCount());
        int total = failures + successes;
        if (total <= 0) {
            return true;
        }
        double rate = (double) failures / (double) total;
        return rate <= MAX_ERROR_RATE;
    }

    private static boolean isFastRecent(CourseQuestionHistory previousHistory) {
        if (previousHistory == null) {
            return false;
        }
        int ms = previousHistory.getResponseTimeMs();
        return ms > 0 && ms <= FAST_RESPONSE_TIME_MS;
    }

    private static QuizFormat chooseMultiTargetFormat(CourseQuestionHistory previousHistory) {
        int round = previousHistory != null ? previousHistory.getQuestionRound() : 0;
        return (round % 2 == 0) ? QuizFormat.ORDERING : QuizFormat.ASSOCIATION;
    }

    private static Integer resolveTimeLimitMs(boolean timed, Integer requested) {
        if (!timed) {
            return null;
        }
        if (requested == null || requested < 1000) {
            return DEFAULT_TIME_LIMIT_MS;
        }
        return requested;
    }

    private static Plan applyTimingOverride(
            Plan plan,
            Boolean requestedTimed,
            Integer requestedTimeLimitMs) {
        if (requestedTimed == null) {
            return plan;
        }
        boolean timed = requestedTimed;
        Integer timeLimitMs = resolveTimeLimitMs(timed, requestedTimeLimitMs);
        return new Plan(plan.format(), timed, timeLimitMs, plan.targetCount(), plan.paramsJson(),
                plan.reason() + "_TIMING_OVERRIDE");
    }

    private static Plan mcqPlan(boolean timed, Integer timeLimitMs, String reason) {
        return new Plan(QuizFormat.MCQ, timed, timeLimitMs, 1, "{\"nbChoices\":4}", reason);
    }
}
