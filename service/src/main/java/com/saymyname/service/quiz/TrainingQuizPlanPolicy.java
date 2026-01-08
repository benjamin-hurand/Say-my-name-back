package com.saymyname.service.quiz;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.course.KnowledgeStats;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.enums.quiz.QuizDecisionReasonCode;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.enums.quiz.QuizPreferredFormat;

@Component
public class TrainingQuizPlanPolicy {

    private static final int DEFAULT_TIME_LIMIT_MS = 8000;
    private static final int FAST_RESPONSE_TIME_MS = 8000;
    private static final int MIN_FLUENT_ATTEMPTS = 3;
    private static final int MAX_ERROR_STREAK_FOR_FLUENCY = 0;
    private static final double MAX_HELP_RECENT_FOR_FLUENCY = 0.5;
    private static final double MAX_ERROR_RATE = 0.2;
    private static final int STRESS_ERROR_STREAK = 2;
    private static final double STRESS_HELP_RECENT = 1.0;
    private static final double SLOW_AVG_RT_MS = 9000;
    private static final int MAX_REASON_DETAILS_CHARS = 4096;

    public record Plan(
            QuizFormat format,
            boolean timed,
            Integer timeLimitMs,
            String paramsJson,
            QuizDecisionReasonCode reasonCode,
            String reasonDetailsJson) {
        public Plan {
            if (format == null) {
                throw new IllegalArgumentException("format is required");
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
            Knowledge knowledge,
            KnowledgeStats stats,
            QuizPreferredFormat preferredFormatQuery,
            Boolean requestedTimed,
            Integer requestedTimeLimitMs) {

        QuizPreferredFormat explicit = normalizePreferred(preferredFormatQuery);
        if (explicit != null) {
            QuizFormat fmt = QuizFormat.valueOf(explicit.name());
            return explicitPlan(fmt, requestedTimed, requestedTimeLimitMs);
        }

        return decideAuto(knowledge, stats, requestedTimed, requestedTimeLimitMs);
    }

    private Plan explicitPlan(
            QuizFormat format,
            Boolean requestedTimed,
            Integer requestedTimeLimitMs) {
        boolean timed = requestedTimed != null && requestedTimed;
        Integer timeLimitMs = resolveTimeLimitMs(timed, requestedTimeLimitMs);
        String paramsJson = (format == QuizFormat.MCQ) ? "{\"nbChoices\":4}" : null;
        String details = new JsonBuilder()
                .add("preferred_format", format.name())
                .build();
        Plan plan = new Plan(format, timed, timeLimitMs, paramsJson,
                QuizDecisionReasonCode.TRAINING_EXPLICIT_USER, details);
        return applyTimingOverride(plan, requestedTimed, requestedTimeLimitMs);
    }

    private Plan decideAuto(
            Knowledge knowledge,
            KnowledgeStats stats,
            Boolean requestedTimed,
            Integer requestedTimeLimitMs) {

        KnowledgeStatus status = knowledge != null && knowledge.getStatus() != null
                ? knowledge.getStatus()
                : KnowledgeStatus.UNKNOWN;

        boolean underStress = isUnderStress(stats);
        boolean fluent = isFluent(stats);
        boolean lowErrorRate = hasLowErrorRate(knowledge, stats);

        if (status == KnowledgeStatus.UNKNOWN || status == KnowledgeStatus.DISCOVERED || underStress) {
            String details = baseDetails(knowledge, stats, underStress, fluent, lowErrorRate)
                    .add("decision", "bootstrap")
                    .build();
            Plan plan = mcqPlan(false, null, QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT, details);
            return applyTimingOverride(plan, requestedTimed, requestedTimeLimitMs);
        }

        if (status == KnowledgeStatus.LEARNED) {
            boolean timed = fluent && !underStress;
            Integer timeLimitMs = resolveTimeLimitMs(timed, DEFAULT_TIME_LIMIT_MS);
            QuizFormat fmt = lowErrorRate ? QuizFormat.TEXT_INPUT : QuizFormat.CLOZE;
            String details = baseDetails(knowledge, stats, underStress, fluent, lowErrorRate)
                    .add("decision", "learned")
                    .add("timed", timed)
                    .build();
            Plan plan = new Plan(fmt, timed, timeLimitMs, null, QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT,
                    details);
            return applyTimingOverride(plan, requestedTimed, requestedTimeLimitMs);
        }

        QuizFormat fmt = lowErrorRate ? QuizFormat.CLOZE : QuizFormat.TEXT_INPUT;
        boolean timed = fluent && !underStress;
        Integer timeLimitMs = resolveTimeLimitMs(timed, DEFAULT_TIME_LIMIT_MS);
        String details = baseDetails(knowledge, stats, underStress, fluent, lowErrorRate)
                .add("decision", "mastered")
                .add("timed", timed)
                .build();
        Plan plan = new Plan(fmt, timed, timeLimitMs, null, QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT, details);
        return applyTimingOverride(plan, requestedTimed, requestedTimeLimitMs);
    }

    private static boolean isUnderStress(KnowledgeStats stats) {
        if (stats == null) {
            return false;
        }
        return stats.getErrorStreak() >= STRESS_ERROR_STREAK
                || stats.getHelpRecent() > STRESS_HELP_RECENT
                || stats.getAvgRtRecent() > SLOW_AVG_RT_MS;
    }

    private static boolean isFluent(KnowledgeStats stats) {
        if (stats == null) {
            return false;
        }
        return stats.getAttemptsRecent() >= MIN_FLUENT_ATTEMPTS
                && stats.getErrorStreak() <= MAX_ERROR_STREAK_FOR_FLUENCY
                && stats.getHelpRecent() <= MAX_HELP_RECENT_FOR_FLUENCY
                && stats.getAvgRtRecent() > 0
                && stats.getAvgRtRecent() <= FAST_RESPONSE_TIME_MS;
    }

    private static boolean hasLowErrorRate(Knowledge knowledge, KnowledgeStats stats) {
        if (stats != null && stats.getAttemptsRecent() > 0) {
            double attempts = Math.max(0, stats.getAttemptsRecent());
            double correct = Math.max(0, stats.getCorrectRecent());
            double failures = Math.max(0, attempts - correct);
            double rate = failures / attempts;
            return rate <= MAX_ERROR_RATE;
        }
        if (knowledge == null) {
            return true;
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

    private static QuizPreferredFormat normalizePreferred(QuizPreferredFormat preferred) {
        if (preferred == null || preferred == QuizPreferredFormat.AUTO) {
            return null;
        }
        return preferred;
    }

    private static JsonBuilder baseDetails(
            Knowledge knowledge,
            KnowledgeStats stats,
            boolean underStress,
            boolean fluent,
            boolean lowErrorRate) {

        KnowledgeStatus status = knowledge != null ? knowledge.getStatus() : null;
        JsonBuilder builder = new JsonBuilder()
                .add("status", status != null ? status.name() : null)
                .add("under_stress", underStress)
                .add("fluent", fluent)
                .add("low_error_rate", lowErrorRate);

        if (stats != null) {
            builder.add("error_streak", stats.getErrorStreak())
                    .add("avg_rt_recent", stats.getAvgRtRecent())
                    .add("help_recent", stats.getHelpRecent())
                    .add("attempts_recent", stats.getAttemptsRecent());
        }

        if (knowledge != null) {
            builder.add("srs_streak", knowledge.getSrsStreak());
        }

        return builder;
    }

    private static final class JsonBuilder {
        private final StringBuilder sb = new StringBuilder(128);
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
        return new Plan(plan.format(), timed, timeLimitMs, plan.paramsJson(),
                QuizDecisionReasonCode.TRAINING_EXPLICIT_USER, details);
    }

    private static Plan mcqPlan(
            boolean timed,
            Integer timeLimitMs,
            QuizDecisionReasonCode reasonCode,
            String reasonDetailsJson) {
        return new Plan(QuizFormat.MCQ, timed, timeLimitMs, "{\"nbChoices\":4}", reasonCode, reasonDetailsJson);
    }

    private static String limitReasonDetailsJson(String details) {
        if (details == null || details.length() <= MAX_REASON_DETAILS_CHARS) {
            return details;
        }
        return details.substring(0, MAX_REASON_DETAILS_CHARS);
    }
}
