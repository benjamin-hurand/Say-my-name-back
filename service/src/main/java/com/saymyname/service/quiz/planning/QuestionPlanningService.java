package com.saymyname.service.quiz.planning;

import java.util.List;

import org.springframework.stereotype.Service;

import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.enums.PoolType;
import com.saymyname.core.model.enums.quiz.QuizDecisionReasonCode;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.enums.quiz.QuizPreferredFormat;
import com.saymyname.core.model.quiz.planning.DifficultyProfile;
import com.saymyname.core.model.quiz.planning.LearnerState;
import com.saymyname.core.model.quiz.planning.LearningObjective;
import com.saymyname.core.model.quiz.planning.PlanningContext;
import com.saymyname.core.model.quiz.planning.PlanningRequest;
import com.saymyname.core.model.quiz.planning.QuestionPlan;
import com.saymyname.service.quiz.planning.FormatAndCandidateSelector.FormatAndCandidates;

/**
 * Unified question planning service.
 * Orchestrates the planning process:
 * 1. Assess learner state
 * 2. Select learning objective
 * 3. Map to difficulty profile
 * 4. Coupled format + candidate selection
 * 5. Configure timing
 * 6. Build and return QuestionPlan
 */
@Service
public class QuestionPlanningService {

    private final LearnerStateAssessor learnerStateAssessor;
    private final ObjectiveSelector objectiveSelector;
    private final FormatAndCandidateSelector formatAndCandidateSelector;
    private final MultiTargetPlanner multiTargetPlanner;

    public QuestionPlanningService(
            LearnerStateAssessor learnerStateAssessor,
            ObjectiveSelector objectiveSelector,
            FormatAndCandidateSelector formatAndCandidateSelector,
            MultiTargetPlanner multiTargetPlanner) {
        this.learnerStateAssessor = learnerStateAssessor;
        this.objectiveSelector = objectiveSelector;
        this.formatAndCandidateSelector = formatAndCandidateSelector;
        this.multiTargetPlanner = multiTargetPlanner;
    }

    /**
     * Plan a question based on the request.
     */
    public QuestionPlan plan(PlanningRequest request) {
        PlanningContext context = request.context();

        // PHASE 1: ASSESS LEARNER STATE
        LearnerState state = learnerStateAssessor.assess(request);

        // PHASE 2: CHECK FOR EXPLICIT USER PREFERENCE
        if (isExplicitFormat(request.preferredFormat())) {
            return planWithExplicitFormat(request, state);
        }

        // PHASE 3: SELECT LEARNING OBJECTIVE
        LearningObjective objective = objectiveSelector.select(state, request);

        // PHASE 3b: MULTI-TARGET GATING (course only)
        MultiTargetPlanner.MultiTargetSelection targetSelection = multiTargetPlanner.selectTargets(
                request,
                objective.requiresMultiTarget());
        PlanningContext effectiveContext = context;
        PlanningRequest effectiveRequest = request;
        if (targetSelection.reasonCodeOverride() != null) {
            effectiveContext = disableMultiTarget(context);
            effectiveRequest = withContext(request, effectiveContext);
            objective = objectiveSelector.select(state, effectiveRequest);
        }

        // PHASE 4: MAP TO DIFFICULTY PROFILE
        DifficultyProfile difficultyProfile = DifficultyProfile.forObjective(objective, effectiveContext);

        // PHASE 5: COUPLED FORMAT + CANDIDATE SELECTION
        FormatAndCandidates selection = formatAndCandidateSelector.select(
                difficultyProfile, state, effectiveRequest, objective);

        // PHASE 6: CONFIGURE TIMING
        boolean timed = difficultyProfile.shouldEnableTiming(request.requestedTimed());
        int timeLimitMs = difficultyProfile.getTimeLimitMs(request.requestedTimeLimitMs());

        // PHASE 7: BUILD AND RETURN
        QuizDecisionReasonCode reasonCode = mapObjectiveToReasonCode(objective, effectiveContext);
        if (targetSelection.reasonCodeOverride() != null) {
            reasonCode = targetSelection.reasonCodeOverride();
        }

        List<Knowledge> targets = targetSelection.targets();
        int targetCount = targetSelection.multiTarget() ? targetSelection.targetCount() : 1;
        String reasonDetailsJson = buildReasonDetailsJson(
                effectiveContext,
                objective,
                difficultyProfile,
                selection,
                request.selectedPoolType(),
                targetCount,
                targetSelection.reasonDetailsJson());

        return QuestionPlan.builder()
                .objective(objective)
                .difficultyProfile(difficultyProfile)
                .format(selection.format())
                .primaryCandidate(selection.primaryCandidate())
                .selectedDistractors(selection.distractors())
                .candidatePoolIds(selection.candidatePoolIds())
                .targetKnowledges(targets)
                .targetCount(targetCount)
                .timed(timed)
                .timeLimitMs(timed ? timeLimitMs : null)
                .reasonCode(reasonCode)
                .difficultyRationale(difficultyProfile.rationale())
                .reasonDetailsJson(reasonDetailsJson)
                .distractorStrategy(selection.selectionStrategy())
                .selectedPoolType(request.selectedPoolType())
                .build();
    }

    /**
     * Check if the preferred format is an explicit user choice (not AUTO).
     */
    private boolean isExplicitFormat(QuizPreferredFormat preferred) {
        return preferred != null && preferred != QuizPreferredFormat.AUTO;
    }

    /**
     * Plan with explicit user-selected format.
     */
    private QuestionPlan planWithExplicitFormat(PlanningRequest request, LearnerState state) {
        PlanningContext context = request.context();

        QuizFormat format = mapPreferredToFormat(request.preferredFormat());

        if (request.preferredFormat() == QuizPreferredFormat.SPEED) {
            format = QuizFormat.MCQ;
        }

        boolean multiTargetRequested = format == QuizFormat.ORDERING || format == QuizFormat.ASSOCIATION;
        MultiTargetPlanner.MultiTargetSelection targetSelection = multiTargetPlanner.selectTargets(
                request,
                multiTargetRequested);

        PlanningContext effectiveContext = context;
        PlanningRequest effectiveRequest = request;
        if (targetSelection.reasonCodeOverride() != null) {
            effectiveContext = disableMultiTarget(context);
            effectiveRequest = withContext(request, effectiveContext);
            if (multiTargetRequested) {
                format = QuizFormat.MCQ;
            }
        }

        if (!effectiveContext.isFormatAllowed(format)) {
            format = effectiveContext.allowedFormats() != null && !effectiveContext.allowedFormats().isEmpty()
                    ? effectiveContext.allowedFormats().iterator().next()
                    : QuizFormat.MCQ;
        }

        // Use a neutral difficulty profile for explicit format
        DifficultyProfile difficultyProfile = new DifficultyProfile(
                0.5, "explicit_user", true, 6000);

        FormatAndCandidates selection = formatAndCandidateSelector.selectWithFormat(
                format, difficultyProfile, state, effectiveRequest, LearningObjective.PRACTICE_LEARNED);

        boolean isSpeed = request.preferredFormat() == QuizPreferredFormat.SPEED;
        boolean timed = isSpeed || (request.requestedTimed() != null && request.requestedTimed());
        int timeLimitMs = isSpeed ? 5000 : difficultyProfile.getTimeLimitMs(request.requestedTimeLimitMs());

        QuizDecisionReasonCode reasonCode = context.isTraining()
                ? QuizDecisionReasonCode.TRAINING_EXPLICIT_USER
                : QuizDecisionReasonCode.EXPLICIT_USER;

        List<Knowledge> targets = targetSelection.targets();
        int targetCount = targetSelection.multiTarget() ? targetSelection.targetCount() : 1;
        String reasonDetailsJson = buildReasonDetailsJson(
                effectiveContext,
                LearningObjective.PRACTICE_LEARNED,
                difficultyProfile,
                selection,
                request.selectedPoolType(),
                targetCount,
                targetSelection.reasonDetailsJson());

        return QuestionPlan.builder()
                .objective(LearningObjective.PRACTICE_LEARNED)
                .difficultyProfile(difficultyProfile)
                .format(format)
                .primaryCandidate(selection.primaryCandidate())
                .selectedDistractors(selection.distractors())
                .candidatePoolIds(selection.candidatePoolIds())
                .targetKnowledges(targets)
                .targetCount(targetCount)
                .timed(timed)
                .timeLimitMs(timed ? timeLimitMs : null)
                .reasonCode(reasonCode)
                .difficultyRationale("explicit_user_preference")
                .reasonDetailsJson(reasonDetailsJson)
                .distractorStrategy(selection.selectionStrategy())
                .selectedPoolType(request.selectedPoolType())
                .build();
    }

    /**
     * Map QuizPreferredFormat to QuizFormat.
     */
    private QuizFormat mapPreferredToFormat(QuizPreferredFormat preferred) {
        return switch (preferred) {
            case TEXT_INPUT -> QuizFormat.TEXT_INPUT;
            case CLOZE -> QuizFormat.CLOZE;
            case HANGMAN -> QuizFormat.HANGMAN;
            case WORD_PUZZLE -> QuizFormat.WORD_PUZZLE;
            case MCQ -> QuizFormat.MCQ;
            case BINARY_SWIPE -> QuizFormat.BINARY_SWIPE;
            case ASSOCIATION -> QuizFormat.ASSOCIATION;
            case ORDERING -> QuizFormat.ORDERING;
            case SPEED -> QuizFormat.MCQ;
            case AUTO -> QuizFormat.MCQ;
        };
    }

    /**
     * Map learning objective to reason code for analytics.
     */
    private QuizDecisionReasonCode mapObjectiveToReasonCode(
            LearningObjective objective,
            PlanningContext context) {

        if (context.isTraining()) {
            return QuizDecisionReasonCode.TRAINING_AUTO_DEFAULT;
        }

        return switch (objective) {
            case INTRODUCE_NEW_EASY, INTRODUCE_NEW_MEDIUM ->
                    QuizDecisionReasonCode.AUTO_UNKNOWN_BOOTSTRAP;
            case REINFORCE_STRUGGLING ->
                    QuizDecisionReasonCode.AUTO_DISCOVERED_BOOTSTRAP;
            case PRACTICE_LEARNED ->
                    QuizDecisionReasonCode.AUTO_LEARNED;
            case PRACTICE_MASTERED ->
                    QuizDecisionReasonCode.AUTO_MASTERED_MULTI_TARGET;
            case CHALLENGE_READY ->
                    QuizDecisionReasonCode.AUTO_MASTERED_MULTI_TARGET;
            case SPEED_DRILL ->
                    QuizDecisionReasonCode.AUTO_LEARNED_FLUENCY_TIMED;
            case CONFIDENCE_BOOST ->
                    QuizDecisionReasonCode.AUTO_LEARNED_EASY;
            case VARIETY_BREAK ->
                    QuizDecisionReasonCode.ANTI_MONOTONY_FORMAT_VARIATION;
            case SRS_REVIEW_DUE, SRS_OVERDUE_URGENT ->
                    QuizDecisionReasonCode.AUTO_MASTERED_SINGLE;
        };
    }

    private String buildReasonDetailsJson(
            PlanningContext context,
            LearningObjective objective,
            DifficultyProfile difficultyProfile,
            FormatAndCandidates selection,
            PoolType poolType,
            int targetCount,
            String multiTargetDetailsJson) {
        return new JsonBuilder()
                .add("objective", objective != null ? objective.name() : null)
                .add("target_difficulty", difficultyProfile != null ? difficultyProfile.targetDifficulty() : null)
                .add("format", selection != null && selection.format() != null ? selection.format().name() : null)
                .add("distractor_strategy", selection != null && selection.selectionStrategy() != null
                        ? selection.selectionStrategy().name()
                        : null)
                .add("context_type", context != null ? context.contextType() : null)
                .add("pool_type", poolType != null ? poolType.name() : null)
                .add("multi_target", targetCount > 1)
                .add("target_count", targetCount)
                .addRaw("multi_target_details", multiTargetDetailsJson)
                .build();
    }

    private PlanningContext disableMultiTarget(PlanningContext context) {
        return new PlanningContext(
                context.contextType(),
                context.srsEnabled(),
                context.knowledgeTracking(),
                false,
                context.sessionTracking(),
                context.allowedFormats(),
                context.difficultyRange(),
                context.courseId(),
                context.challengeId(),
                context.metadata());
    }

    private PlanningRequest withContext(PlanningRequest request, PlanningContext context) {
        return PlanningRequest.builder()
                .userId(request.userId())
                .gameModeId(request.gameModeId())
                .populationScope(request.populationScope())
                .gameOptions(request.gameOptions())
                .context(context)
                .course(request.course())
                .primaryKnowledge(request.primaryKnowledge())
                .selectedPoolType(request.selectedPoolType())
                .sessionStats(request.sessionStats())
                .preferredFormat(request.preferredFormat())
                .requestedTimed(request.requestedTimed())
                .requestedTimeLimitMs(request.requestedTimeLimitMs())
                .lastPersonId(request.lastPersonId())
                .lastCorrect(request.lastCorrect())
                .build();
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
}
