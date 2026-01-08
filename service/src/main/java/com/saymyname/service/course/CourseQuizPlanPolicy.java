// src/main/java/com/saymyname/service/course/CourseQuizPlanPolicy.java
package com.saymyname.service.course;

import org.springframework.stereotype.Component;

import java.util.List;

import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.CourseRecentStats;
import com.saymyname.core.model.course.CourseQuestionHistory;
import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.course.KnowledgeStats;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.enums.quiz.QuizDecisionReasonCode;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.enums.quiz.QuizPreferredFormat;

@Component
public class CourseQuizPlanPolicy {

    private static final int MIN_TIMED_STREAK = 2;
    private static final int FAST_RESPONSE_TIME_MS = 8000;
    private static final int DEFAULT_TIME_LIMIT_MS = 8000;
    private static final double MAX_ERROR_RATE = 0.2;
    private static final int MULTI_TARGET_MIN = 4;
    private static final int MULTI_TARGET_MAX_ERROR_STREAK = 0;
    private static final int MULTI_TARGET_MAX_AVG_RT_MS = 9000;
    private static final double MULTI_TARGET_MAX_HELP_RECENT = 1.0;
    private static final double MULTI_TARGET_MIN_ATTEMPTS_RECENT = 1.0;
    private static final int MULTI_TARGET_ORDERING_RT_THRESHOLD_MS = 8000;
    private static final double MULTI_TARGET_ORDERING_HELP_THRESHOLD = 0.7;
    private static final int FORMAT_VARIATION_STREAK = 3;
    private static final int COURSE_STRESS_ERROR_STREAK = 2;
    private static final int COURSE_STRESS_HELP_STREAK = 2;
    private static final int KNOWLEDGE_STRESS_ERROR_STREAK = 1;
    private static final double KNOWLEDGE_STRESS_HELP_RECENT = 1.0;
    private static final double KNOWLEDGE_SLOW_RT_MS = 9000;
    private static final int MAX_REASON_DETAILS_CHARS = 4096;

    public record Plan(
            QuizFormat format,
            boolean timed,
            Integer timeLimitMs,
            int targetCount,
            String paramsJson,
            QuizDecisionReasonCode reasonCode,
            String reasonDetailsJson) {
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
            if (reasonCode == null) {
                throw new IllegalArgumentException("reasonCode is required");
            }
            reasonDetailsJson = limitReasonDetailsJson(reasonDetailsJson);
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
        return decide(
                course,
                previousHistory,
                nextKnowledge,
                null,
                null,
                null,
                preferredFormatQuery,
                requestedTimed,
                requestedTimeLimitMs,
                multiTargetAvailable);
    }

    public Plan decide(
            Course course,
            CourseQuestionHistory previousHistory,
            Knowledge nextKnowledge,
            KnowledgeStats primaryStats,
            List<KnowledgeStats> extraStats,
            CourseRecentStats courseStats,
            QuizPreferredFormat preferredFormatQuery,
            Boolean requestedTimed,
            Integer requestedTimeLimitMs,
            boolean multiTargetAvailable) {

        QuizPreferredFormat explicit = normalizePreferred(preferredFormatQuery);
        if (explicit != null) {
            QuizFormat fmt = QuizFormat.valueOf(explicit.name());
            return explicitPlan(fmt, requestedTimed, requestedTimeLimitMs, multiTargetAvailable);
        }

        return decideAuto(course, previousHistory, nextKnowledge, primaryStats, extraStats, courseStats, requestedTimed,
                requestedTimeLimitMs, multiTargetAvailable);
    }

    private Plan explicitPlan(
            QuizFormat format,
            Boolean requestedTimed,
            Integer requestedTimeLimitMs,
            boolean multiTargetAvailable) {
        int targetCount = (format == QuizFormat.ORDERING || format == QuizFormat.ASSOCIATION)
                ? MULTI_TARGET_MIN
                : 1;
        boolean timed = targetCount > 1 ? false : (requestedTimed != null && requestedTimed);
        Integer timeLimitMs = resolveTimeLimitMs(timed, requestedTimeLimitMs);
        String details = new JsonBuilder()
                .add("preferred_format", format.name())
                .add("target_count", targetCount)
                .add("multi_target_available", multiTargetAvailable)
                .build();
        if (targetCount > 1 && !multiTargetAvailable) {
            String fallbackDetails = new JsonBuilder()
                    .add("preferred_format", format.name())
                    .add("target_count", targetCount)
                    .add("multi_target_available", multiTargetAvailable)
                    .add("fallback_format", QuizFormat.MCQ.name())
                    .add("fallback_reason", "multi_target_unavailable")
                    .build();
            Plan fallback = mcqPlan(false, null, QuizDecisionReasonCode.EXPLICIT_USER, fallbackDetails);
            return applyTimingOverride(fallback, requestedTimed, requestedTimeLimitMs);
        }
        String paramsJson = (format == QuizFormat.MCQ) ? "{\"nbChoices\":4}" : null;
        Plan plan = new Plan(format, timed, timeLimitMs, targetCount, paramsJson,
                QuizDecisionReasonCode.EXPLICIT_USER, details);
        return applyTimingOverride(plan, requestedTimed, requestedTimeLimitMs);
    }

    private Plan decideAuto(
            Course course,
            CourseQuestionHistory previousHistory,
            Knowledge nextKnowledge,
            KnowledgeStats primaryStats,
            List<KnowledgeStats> extraStats,
            CourseRecentStats courseStats,
            Boolean requestedTimed,
            Integer requestedTimeLimitMs,
            boolean multiTargetAvailable) {

        if (nextKnowledge == null) {
            throw new IllegalStateException("CourseQuizPlanPolicy requires target Knowledge");
        }

        KnowledgeStatus status = nextKnowledge.getStatus() != null
                ? nextKnowledge.getStatus()
                : KnowledgeStatus.UNKNOWN;

        boolean courseUnderStress = isCourseUnderStress(courseStats);
        boolean knowledgeUnderStress = isKnowledgeUnderStress(primaryStats);
        boolean avoidTimed = courseUnderStress || knowledgeUnderStress;
        boolean userUnderStress = courseUnderStress || knowledgeUnderStress;

        if (status == KnowledgeStatus.UNKNOWN || status == KnowledgeStatus.DISCOVERED) {
            QuizDecisionReasonCode reasonCode = status == KnowledgeStatus.DISCOVERED
                    ? QuizDecisionReasonCode.AUTO_DISCOVERED_BOOTSTRAP
                    : QuizDecisionReasonCode.AUTO_UNKNOWN_BOOTSTRAP;
            String details = baseDetails(nextKnowledge, primaryStats, courseStats, courseUnderStress,
                    knowledgeUnderStress)
                    .add("decision", "bootstrap")
                    .build();
            Plan plan = mcqPlan(false, null, reasonCode, details);
            plan = applyFormatVariationIfNeeded(plan, courseStats, userUnderStress);
            return applyTimingOverride(plan, requestedTimed, requestedTimeLimitMs);
        }

        if (status == KnowledgeStatus.LEARNED) {
            boolean streakOk = nextKnowledge.getSrsStreak() >= MIN_TIMED_STREAK;
            boolean fast = isFastRecent(previousHistory, primaryStats);
            boolean timed = !avoidTimed && streakOk && fast;
            Integer timeLimitMs = resolveTimeLimitMs(timed, DEFAULT_TIME_LIMIT_MS);
            QuizFormat fmt = avoidTimed ? QuizFormat.MCQ : QuizFormat.TEXT_INPUT;
            String paramsJson = (fmt == QuizFormat.MCQ) ? "{\"nbChoices\":4}" : null;
            QuizDecisionReasonCode reasonCode = timed
                    ? QuizDecisionReasonCode.AUTO_LEARNED_FLUENCY_TIMED
                    : (avoidTimed ? QuizDecisionReasonCode.AUTO_LEARNED_EASY : QuizDecisionReasonCode.AUTO_LEARNED);
            String details = baseDetails(nextKnowledge, primaryStats, courseStats, courseUnderStress,
                    knowledgeUnderStress)
                    .add("streak_ok", streakOk)
                    .add("fast_recent", fast)
                    .add("timed", timed)
                    .build();
            Plan plan = new Plan(fmt, timed, timeLimitMs, 1, paramsJson, reasonCode, details);
            plan = applyFormatVariationIfNeeded(plan, courseStats, userUnderStress);
            return applyTimingOverride(plan, requestedTimed, requestedTimeLimitMs);
        }

        boolean lowErrorRate = hasLowErrorRate(nextKnowledge, primaryStats);
        boolean fast = isFastRecent(previousHistory, primaryStats);
        boolean timed = !avoidTimed && fast && lowErrorRate;

        if (status == KnowledgeStatus.MASTERED && multiTargetAvailable && !avoidTimed && lowErrorRate) {
            MultiTargetGate gate = evaluateMultiTargetGate(primaryStats, extraStats);
            boolean gateOk = gate.passed();
            JsonBuilder gateDetails = baseDetails(nextKnowledge, primaryStats, courseStats, courseUnderStress,
                    knowledgeUnderStress)
                    .add("multi_target_available", multiTargetAvailable)
                    .add("low_error_rate", lowErrorRate)
                    .add("fast_recent", fast)
                    .add("multi_target_gate_passed", gateOk)
                    .addRaw("multi_target_gate", gate.detailsJson());

            if (gateOk) {
                QuizFormat fmt = chooseMultiTargetFormat(previousHistory, courseStats, gate);
                gateDetails.add("multi_target_format", fmt.name());
                Plan plan = new Plan(fmt, false, null, MULTI_TARGET_MIN, null,
                        QuizDecisionReasonCode.AUTO_MASTERED_MULTI_TARGET, gateDetails.build());
                plan = applyFormatVariationIfNeeded(plan, courseStats, userUnderStress);
                return applyTimingOverride(plan, requestedTimed, requestedTimeLimitMs);
            }

            QuizFormat fallbackFmt = (avoidTimed || !lowErrorRate) ? QuizFormat.TEXT_INPUT : QuizFormat.CLOZE;
            Integer timeLimitMs = resolveTimeLimitMs(timed, DEFAULT_TIME_LIMIT_MS);
            Plan plan = new Plan(fallbackFmt, timed, timeLimitMs, 1, null,
                    QuizDecisionReasonCode.FALLBACK_MULTI_TARGET_GATE, gateDetails.build());
            plan = applyFormatVariationIfNeeded(plan, courseStats, userUnderStress);
            return applyTimingOverride(plan, requestedTimed, requestedTimeLimitMs);
        }

        QuizFormat fmt = (avoidTimed || !lowErrorRate) ? QuizFormat.TEXT_INPUT : QuizFormat.CLOZE;
        Integer timeLimitMs = resolveTimeLimitMs(timed, DEFAULT_TIME_LIMIT_MS);
        String details = baseDetails(nextKnowledge, primaryStats, courseStats, courseUnderStress, knowledgeUnderStress)
                .add("low_error_rate", lowErrorRate)
                .add("fast_recent", fast)
                .add("timed", timed)
                .build();
        Plan plan = new Plan(fmt, timed, timeLimitMs, 1, null, QuizDecisionReasonCode.AUTO_MASTERED_SINGLE, details);
        plan = applyFormatVariationIfNeeded(plan, courseStats, userUnderStress);
        return applyTimingOverride(plan, requestedTimed, requestedTimeLimitMs);
    }

    private static QuizPreferredFormat normalizePreferred(QuizPreferredFormat preferred) {
        if (preferred == null || preferred == QuizPreferredFormat.AUTO) {
            return null;
        }
        return preferred;
    }

    private static boolean hasLowErrorRate(Knowledge knowledge) {
        return hasLowErrorRate(knowledge, null);
    }

    private static boolean hasLowErrorRate(Knowledge knowledge, KnowledgeStats stats) {
        if (stats != null && stats.getAttemptsRecent() > 0) {
            double attempts = Math.max(0, stats.getAttemptsRecent());
            double correct = Math.max(0, stats.getCorrectRecent());
            double failures = Math.max(0, attempts - correct);
            double rate = failures / attempts;
            return rate <= MAX_ERROR_RATE;
        }
        int failures = Math.max(0, knowledge.getFailureCount());
        int successes = Math.max(0, knowledge.getSuccessCount());
        int total = failures + successes;
        if (total <= 0) {
            return true;
        }
        double rate = (double) failures / (double) total;
        return rate <= MAX_ERROR_RATE;
    }

    private static boolean isFastRecent(CourseQuestionHistory previousHistory, KnowledgeStats stats) {
        if (previousHistory == null) {
            if (stats == null) {
                return false;
            }
            double avgRt = stats.getAvgRtRecent();
            return avgRt > 0 && avgRt <= FAST_RESPONSE_TIME_MS;
        }
        int ms = previousHistory.getResponseTimeMs();
        return ms > 0 && ms <= FAST_RESPONSE_TIME_MS;
    }

    private static boolean isCourseUnderStress(CourseRecentStats stats) {
        if (stats == null) {
            return false;
        }
        return stats.getErrorStreak() >= COURSE_STRESS_ERROR_STREAK
                || stats.getHelpStreak() >= COURSE_STRESS_HELP_STREAK;
    }

    private static boolean isKnowledgeUnderStress(KnowledgeStats stats) {
        if (stats == null) {
            return false;
        }
        return stats.getErrorStreak() >= KNOWLEDGE_STRESS_ERROR_STREAK
                || stats.getHelpRecent() > KNOWLEDGE_STRESS_HELP_RECENT
                || stats.getAvgRtRecent() > KNOWLEDGE_SLOW_RT_MS;
    }

    private static MultiTargetGate evaluateMultiTargetGate(
            KnowledgeStats primaryStats,
            List<KnowledgeStats> extraStats) {

        int maxErrorStreak = 0;
        double maxAvgRtRecent = 0;
        double maxHelpRecent = 0;
        double minAttemptsRecent = Double.MAX_VALUE;
        int considered = 0;
        int missing = 0;

        if (primaryStats == null) {
            missing++;
        } else {
            considered++;
            maxErrorStreak = Math.max(maxErrorStreak, primaryStats.getErrorStreak());
            maxAvgRtRecent = Math.max(maxAvgRtRecent, primaryStats.getAvgRtRecent());
            maxHelpRecent = Math.max(maxHelpRecent, primaryStats.getHelpRecent());
            minAttemptsRecent = Math.min(minAttemptsRecent, primaryStats.getAttemptsRecent());
        }

        if (extraStats != null) {
            for (KnowledgeStats stats : extraStats) {
                if (stats == null) {
                    missing++;
                    continue;
                }
                considered++;
                maxErrorStreak = Math.max(maxErrorStreak, stats.getErrorStreak());
                maxAvgRtRecent = Math.max(maxAvgRtRecent, stats.getAvgRtRecent());
                maxHelpRecent = Math.max(maxHelpRecent, stats.getHelpRecent());
                minAttemptsRecent = Math.min(minAttemptsRecent, stats.getAttemptsRecent());
            }
        }

        if (considered == 0) {
            minAttemptsRecent = 0;
        }

        return new MultiTargetGate(
                maxErrorStreak,
                maxAvgRtRecent,
                maxHelpRecent,
                minAttemptsRecent,
                considered,
                missing);
    }

    private static QuizFormat chooseMultiTargetFormat(
            CourseQuestionHistory previousHistory,
            CourseRecentStats stats,
            MultiTargetGate gate) {

        if (gate == null || gate.consideredCount() == 0) {
            int round = previousHistory != null ? previousHistory.getQuestionRound() : 0;
            return (round % 2 == 0) ? QuizFormat.ORDERING : QuizFormat.ASSOCIATION;
        }

        boolean preferOrdering = gate.maxErrorStreak() > 0
                || gate.maxAvgRtRecent() >= MULTI_TARGET_ORDERING_RT_THRESHOLD_MS
                || gate.maxHelpRecent() >= MULTI_TARGET_ORDERING_HELP_THRESHOLD;
        QuizFormat candidate = preferOrdering ? QuizFormat.ORDERING : QuizFormat.ASSOCIATION;

        if (stats != null && stats.getLastFormat() != null && stats.getFormatStreak() >= 2
                && stats.getLastFormat() == candidate) {
            return candidate == QuizFormat.ORDERING ? QuizFormat.ASSOCIATION : QuizFormat.ORDERING;
        }

        return candidate;
    }

    private static Plan applyFormatVariationIfNeeded(
            Plan plan,
            CourseRecentStats courseStats,
            boolean underStress) {
        if (plan == null || courseStats == null || underStress
                || courseStats.getFormatStreak() < FORMAT_VARIATION_STREAK) {
            return plan;
        }
        QuizFormat lastFormat = courseStats.getLastFormat();
        if (lastFormat == null || lastFormat != plan.format()) {
            return plan;
        }
        if (plan.targetCount() > 1) {
            if (plan.format() != QuizFormat.ORDERING && plan.format() != QuizFormat.ASSOCIATION) {
                return plan;
            }
            QuizFormat forced = plan.format() == QuizFormat.ORDERING ? QuizFormat.ASSOCIATION : QuizFormat.ORDERING;
            String details = new JsonBuilder()
                    .add("format_streak", courseStats.getFormatStreak())
                    .add("last_format", lastFormat.name())
                    .add("forced_format", forced.name())
                    .add("variation_scope", "multi_target")
                    .add("base_reason_code", plan.reasonCode().name())
                    .addRaw("base_reason_details", plan.reasonDetailsJson())
                    .build();

            return new Plan(forced, plan.timed(), plan.timeLimitMs(), plan.targetCount(), plan.paramsJson(),
                    QuizDecisionReasonCode.ANTI_MONOTONY_FORMAT_VARIATION, details);
        }

        if (plan.format() != QuizFormat.MCQ && plan.format() != QuizFormat.CLOZE
                && plan.format() != QuizFormat.TEXT_INPUT) {
            return plan;
        }

        QuizFormat forced = nextFormatForVariation(plan.format());
        String paramsJson = (forced == QuizFormat.MCQ) ? "{\"nbChoices\":4}" : null;
        String details = new JsonBuilder()
                .add("format_streak", courseStats.getFormatStreak())
                .add("last_format", lastFormat.name())
                .add("forced_format", forced.name())
                .add("variation_scope", "single_target")
                .add("base_reason_code", plan.reasonCode().name())
                .addRaw("base_reason_details", plan.reasonDetailsJson())
                .build();

        return new Plan(forced, plan.timed(), plan.timeLimitMs(), plan.targetCount(), paramsJson,
                QuizDecisionReasonCode.ANTI_MONOTONY_FORMAT_VARIATION, details);
    }

    private static QuizFormat nextFormatForVariation(QuizFormat current) {
        if (current == QuizFormat.MCQ) {
            return QuizFormat.CLOZE;
        }
        if (current == QuizFormat.CLOZE) {
            return QuizFormat.TEXT_INPUT;
        }
        return QuizFormat.MCQ;
    }

    private static JsonBuilder baseDetails(
            Knowledge knowledge,
            KnowledgeStats primaryStats,
            CourseRecentStats courseStats,
            boolean courseUnderStress,
            boolean knowledgeUnderStress) {

        KnowledgeStatus status = knowledge != null ? knowledge.getStatus() : null;
        JsonBuilder builder = new JsonBuilder()
                .add("status", status != null ? status.name() : null)
                .add("srs_streak", knowledge != null ? knowledge.getSrsStreak() : null)
                .add("course_under_stress", courseUnderStress)
                .add("knowledge_under_stress", knowledgeUnderStress);

        if (primaryStats != null) {
            builder.add("error_streak", primaryStats.getErrorStreak())
                    .add("avg_rt_recent", primaryStats.getAvgRtRecent())
                    .add("help_recent", primaryStats.getHelpRecent())
                    .add("attempts_recent", primaryStats.getAttemptsRecent());
        }

        if (courseStats != null) {
            builder.add("format_streak", courseStats.getFormatStreak())
                    .add("last_format", courseStats.getLastFormat() != null
                            ? courseStats.getLastFormat().name()
                            : null)
                    .add("course_error_streak", courseStats.getErrorStreak())
                    .add("course_help_streak", courseStats.getHelpStreak())
                    .add("course_avg_rt_recent", courseStats.getAvgRtRecent());
        }

        return builder;
    }

    private record MultiTargetGate(
            int maxErrorStreak,
            double maxAvgRtRecent,
            double maxHelpRecent,
            double minAttemptsRecent,
            int consideredCount,
            int missingCount) {
        boolean passed() {
            return consideredCount > 0
                    && missingCount == 0
                    && maxErrorStreak <= MULTI_TARGET_MAX_ERROR_STREAK
                    && maxAvgRtRecent <= MULTI_TARGET_MAX_AVG_RT_MS
                    && maxHelpRecent <= MULTI_TARGET_MAX_HELP_RECENT
                    && minAttemptsRecent >= MULTI_TARGET_MIN_ATTEMPTS_RECENT;
        }

        String detailsJson() {
            return new JsonBuilder()
                    .add("max_error_streak", maxErrorStreak)
                    .add("max_avg_rt_recent", maxAvgRtRecent)
                    .add("max_help_recent", maxHelpRecent)
                    .add("min_attempts_recent", minAttemptsRecent)
                    .add("considered_count", consideredCount)
                    .add("missing_count", missingCount)
                    .build();
        }
    }

    private static final class JsonBuilder {
        private final StringBuilder sb = new StringBuilder(160);
        private boolean first = true;

        private JsonBuilder() {
            sb.append('{');
        }

        JsonBuilder add(String key, String value) {
            if (value == null) {
                return this;
            }
            sep();
            sb.append('"').append(escape(key)).append("\":\"").append(escape(value)).append('"');
            return this;
        }

        JsonBuilder add(String key, Number value) {
            if (value == null) {
                return this;
            }
            sep();
            sb.append('"').append(escape(key)).append("\":").append(value);
            return this;
        }

        JsonBuilder add(String key, Boolean value) {
            if (value == null) {
                return this;
            }
            sep();
            sb.append('"').append(escape(key)).append("\":").append(value);
            return this;
        }

        JsonBuilder addRaw(String key, String rawJson) {
            if (rawJson == null || rawJson.isBlank()) {
                return this;
            }
            sep();
            sb.append('"').append(escape(key)).append("\":").append(rawJson);
            return this;
        }

        String build() {
            sb.append('}');
            return sb.toString();
        }

        private void sep() {
            if (!first) {
                sb.append(',');
            }
            first = false;
        }

        private static String escape(String s) {
            if (s == null) {
                return null;
            }
            StringBuilder out = new StringBuilder(s.length() + 8);
            for (int i = 0; i < s.length(); i++) {
                char c = s.charAt(i);
                if (c == '"' || c == '\\') {
                    out.append('\\');
                }
                out.append(c);
            }
            return out.toString();
        }
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
        if (timed == plan.timed() && java.util.Objects.equals(timeLimitMs, plan.timeLimitMs())) {
            return plan;
        }
        String details = new JsonBuilder()
                .add("requested_timed", requestedTimed)
                .add("requested_time_limit_ms", requestedTimeLimitMs)
                .add("resolved_time_limit_ms", timeLimitMs)
                .add("base_reason_code", plan.reasonCode().name())
                .addRaw("base_reason_details", plan.reasonDetailsJson())
                .add("base_timed", plan.timed())
                .add("base_time_limit_ms", plan.timeLimitMs())
                .build();
        return new Plan(plan.format(), timed, timeLimitMs, plan.targetCount(), plan.paramsJson(),
                QuizDecisionReasonCode.EXPLICIT_USER, details);
    }

    private static Plan mcqPlan(
            boolean timed,
            Integer timeLimitMs,
            QuizDecisionReasonCode reasonCode,
            String reasonDetailsJson) {
        return new Plan(QuizFormat.MCQ, timed, timeLimitMs, 1, "{\"nbChoices\":4}", reasonCode, reasonDetailsJson);
    }

    private static String limitReasonDetailsJson(String details) {
        if (details == null || details.length() <= MAX_REASON_DETAILS_CHARS) {
            return details;
        }
        return details.substring(0, MAX_REASON_DETAILS_CHARS);
    }
}
