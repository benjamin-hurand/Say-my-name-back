package com.saymyname.service.quiz.planning;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.course.KnowledgeStats;
import com.saymyname.core.model.enums.quiz.QuizDecisionReasonCode;
import com.saymyname.core.model.quiz.planning.PlanningRequest;
import com.saymyname.service.course.KnowledgeSelectionService;
import com.saymyname.service.course.KnowledgeStatsService;

@Service
public class MultiTargetPlanner {

    private static final int MULTI_TARGET_MIN = 4;
    private static final int MULTI_TARGET_FETCH_FACTOR = 3;
    private static final int MULTI_TARGET_MAX_ERROR_STREAK = 0;
    private static final double MULTI_TARGET_MAX_AVG_RT_MS = 9000;
    private static final double MULTI_TARGET_MAX_HELP_RECENT = 1.0;
    private static final double MULTI_TARGET_MIN_ATTEMPTS_RECENT = 1.0;

    private final KnowledgeSelectionService knowledgeSelectionService;
    private final KnowledgeStatsService knowledgeStatsService;

    public MultiTargetPlanner(
            KnowledgeSelectionService knowledgeSelectionService,
            KnowledgeStatsService knowledgeStatsService) {
        this.knowledgeSelectionService = knowledgeSelectionService;
        this.knowledgeStatsService = knowledgeStatsService;
    }

    public record MultiTargetSelection(
            boolean multiTarget,
            int targetCount,
            List<Knowledge> targets,
            QuizDecisionReasonCode reasonCodeOverride,
            String reasonDetailsJson
    ) {
    }

    public MultiTargetSelection selectTargets(PlanningRequest request, boolean requireMultiTarget) {
        Knowledge primary = request.primaryKnowledge();

        if (!requireMultiTarget || !request.context().multiTargetAllowed()) {
            return singleTarget(primary);
        }

        Course course = request.course();
        if (course == null || primary == null) {
            return fallback(QuizDecisionReasonCode.FALLBACK_MULTI_TARGET_SHORTFALL, "missing_course_or_primary", primary);
        }

        KnowledgeSelectionService.MultiTargetConstraints constraints = new KnowledgeSelectionService.MultiTargetConstraints(
                MULTI_TARGET_MAX_ERROR_STREAK,
                MULTI_TARGET_MAX_AVG_RT_MS,
                MULTI_TARGET_MAX_HELP_RECENT,
                MULTI_TARGET_MIN_ATTEMPTS_RECENT,
                MULTI_TARGET_FETCH_FACTOR);

        List<Knowledge> extras = knowledgeSelectionService.findNextDueMultiTargets(
                course,
                primary,
                MULTI_TARGET_MIN - 1,
                request.lastPersonId(),
                constraints);

        if (extras.size() < MULTI_TARGET_MIN - 1) {
            return fallback(QuizDecisionReasonCode.FALLBACK_MULTI_TARGET_SHORTFALL, "extra_target_shortfall", primary);
        }

        GateEvaluation gate = evaluateGate(request, primary, extras);
        if (!gate.passed()) {
            return new MultiTargetSelection(false, 1, List.of(primary),
                    QuizDecisionReasonCode.FALLBACK_MULTI_TARGET_GATE, gate.detailsJson());
        }

        List<Knowledge> targets = new ArrayList<>();
        targets.add(primary);
        targets.addAll(extras);
        return new MultiTargetSelection(true, MULTI_TARGET_MIN, targets, null, gate.detailsJson());
    }

    private MultiTargetSelection singleTarget(Knowledge primary) {
        List<Knowledge> targets = primary != null ? List.of(primary) : List.of();
        return new MultiTargetSelection(false, 1, targets, null, null);
    }

    private MultiTargetSelection fallback(
            QuizDecisionReasonCode reasonCode,
            String reason,
            Knowledge primary) {
        String details = new JsonBuilder()
                .add("fallback_reason", reason)
                .add("requested_multi_target", true)
                .build();
        List<Knowledge> targets = primary != null ? List.of(primary) : List.of();
        return new MultiTargetSelection(false, 1, targets, reasonCode, details);
    }

    private GateEvaluation evaluateGate(PlanningRequest request, Knowledge primary, List<Knowledge> extras) {
        if (request.userId() == null || request.gameModeId() == null) {
            return GateEvaluation.missing("missing_user_or_game_mode");
        }

        List<Long> ids = new ArrayList<>();
        if (primary != null && primary.getId() != null) {
            ids.add(primary.getId());
        }
        for (Knowledge k : extras) {
            if (k != null && k.getId() != null) {
                ids.add(k.getId());
            }
        }
        if (ids.isEmpty()) {
            return GateEvaluation.missing("missing_knowledge_ids");
        }

        Map<Long, KnowledgeStats> statsMap = knowledgeStatsService.getStatsForKnowledgeIds(
                request.userId(),
                request.gameModeId(),
                ids);

        int maxErrorStreak = 0;
        double maxAvgRtRecent = 0;
        double maxHelpRecent = 0;
        double minAttemptsRecent = Double.MAX_VALUE;
        int considered = 0;
        int missing = 0;

        for (Long id : ids) {
            KnowledgeStats stats = statsMap.get(id);
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

        if (considered == 0) {
            minAttemptsRecent = 0;
        }

        GateEvaluation gate = new GateEvaluation(
                maxErrorStreak,
                maxAvgRtRecent,
                maxHelpRecent,
                minAttemptsRecent,
                considered,
                missing,
                null);

        return gate;
    }

    private record GateEvaluation(
            int maxErrorStreak,
            double maxAvgRtRecent,
            double maxHelpRecent,
            double minAttemptsRecent,
            int consideredCount,
            int missingCount,
            String failureReason) {

        static GateEvaluation missing(String reason) {
            return new GateEvaluation(0, 0, 0, 0, 0, 0, reason);
        }

        boolean passed() {
            if (failureReason != null) {
                return false;
            }
            return consideredCount > 0
                    && missingCount == 0
                    && maxErrorStreak <= MULTI_TARGET_MAX_ERROR_STREAK
                    && maxAvgRtRecent <= MULTI_TARGET_MAX_AVG_RT_MS
                    && maxHelpRecent <= MULTI_TARGET_MAX_HELP_RECENT
                    && minAttemptsRecent >= MULTI_TARGET_MIN_ATTEMPTS_RECENT;
        }

        String detailsJson() {
            JsonBuilder builder = new JsonBuilder()
                    .add("max_error_streak", maxErrorStreak)
                    .add("max_avg_rt_recent", maxAvgRtRecent)
                    .add("max_help_recent", maxHelpRecent)
                    .add("min_attempts_recent", minAttemptsRecent)
                    .add("considered_count", consideredCount)
                    .add("missing_count", missingCount);
            if (failureReason != null) {
                builder.add("failure_reason", failureReason);
            }
            return builder.build();
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
}
