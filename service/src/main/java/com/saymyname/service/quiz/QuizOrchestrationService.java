// src/main/java/com/saymyname/service/quiz/QuizOrchestrationService.java
package com.saymyname.service.quiz;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.exception.quiz.QuizUnprocessableException;
import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.enums.FollowFilter;
import com.saymyname.core.model.enums.PopulationScope;
import com.saymyname.core.model.enums.PoolType;
import com.saymyname.core.model.enums.quiz.FormatMode;
import com.saymyname.core.model.enums.quiz.QuizQuestionSource;
import com.saymyname.core.model.quiz.QuizQuestionContext;
import com.saymyname.core.model.quiz.QuizQuestionSpec;
import com.saymyname.core.model.quiz.QuizPayloadItem;
import com.saymyname.core.model.quiz.candidate.CandidateQuery;
import com.saymyname.core.model.quiz.candidate.CandidateSample;
import com.saymyname.core.model.quiz.candidate.EligibilityStats;
import com.saymyname.core.model.quiz.candidate.PayloadItem;
import com.saymyname.core.model.quiz.options.CategorySelection;
import com.saymyname.core.model.quiz.options.GameMode;
import com.saymyname.core.model.quiz.options.GameModeAttribute;
import com.saymyname.core.model.quiz.options.TrainingOptions;
import com.saymyname.core.model.quiz.planning.PlanningDecision;
import com.saymyname.core.model.quiz.planning.PlanningRequest;
import com.saymyname.core.model.quiz.planning.PreparedEmit;
import com.saymyname.persistence.dao.course.CourseDao;
import com.saymyname.service.GameModeService;
import com.saymyname.service.course.KnowledgeSelectionService;
import com.saymyname.service.course.KnowledgeSelectionService.SelectionResult;
import com.saymyname.service.quiz.candidate.CandidateAccessor;
import com.saymyname.service.quiz.planning.FormatPlanner;

@Service
public class QuizOrchestrationService {

        private final GameModeService gameModeService;
        private final CourseDao courseDao;
        private final KnowledgeSelectionService knowledgeSelectionService;
        private final CandidateAccessor candidateAccessor;
        private final FormatPlanner formatPlanner;

        public QuizOrchestrationService(
                        GameModeService gameModeService,
                        CourseDao courseDao,
                        KnowledgeSelectionService knowledgeSelectionService,
                        CandidateAccessor candidateAccessor,
                        FormatPlanner formatPlanner) {

                this.gameModeService = Objects.requireNonNull(gameModeService, "gameModeService");
                this.courseDao = Objects.requireNonNull(courseDao, "courseDao");
                this.knowledgeSelectionService = Objects.requireNonNull(knowledgeSelectionService,
                                "knowledgeSelectionService");
                this.candidateAccessor = Objects.requireNonNull(candidateAccessor, "candidateAccessor");
                this.formatPlanner = Objects.requireNonNull(formatPlanner, "formatPlanner");
        }

        // ------------------------------------------------------------------
        // TRAINING
        // ------------------------------------------------------------------
        @Transactional(readOnly = true)
        public PreparedEmit orchestrateTraining(
                        Long userId,
                        TrainingOptions options,
                        FormatMode formatMode,
                        com.saymyname.core.model.enums.quiz.QuizFormat forcedFormat,
                        Boolean timed,
                        Integer timeLimitMs,
                        Long ttlSeconds) {

                Objects.requireNonNull(userId, "userId");
                Objects.requireNonNull(options, "options");
                Objects.requireNonNull(options.getGameModeId(), "options.gameModeId");
                Objects.requireNonNull(formatMode, "formatMode");

                GameMode gameMode = gameModeService.findByIdOrThrow(options.getGameModeId());
                List<Long> targetAttributeIds = extractTargetAttributeIds(gameMode);
                String operator = resolveOperator(gameMode);

                FollowFilter scope = options.getPopulationScope();

                CandidateQuery countQuery = buildCandidateQuery(
                                userId,
                                scope,
                                options.getCategory(),
                                gameMode.getId(),
                                targetAttributeIds,
                                operator,
                                true,
                                false,
                                null);

                EligibilityStats stats = candidateAccessor.countEligibility(countQuery);

                PlanningRequest planningRequest = (formatMode == FormatMode.FORCED)
                                ? PlanningRequest.forced(forcedFormat, stats, gameMode, timed, timeLimitMs)
                                : PlanningRequest.auto(stats, gameMode, timed, timeLimitMs);

                PlanningDecision decision = formatPlanner.plan(planningRequest);

                CandidateQuery sampleQuery = buildCandidateQuery(
                                userId,
                                scope,
                                options.getCategory(),
                                gameMode.getId(),
                                targetAttributeIds,
                                operator,
                                false,
                                decision.requiresPhoto(),
                                decision.sampleSize());

                CandidateSample sample = candidateAccessor.sample(sampleQuery, decision.sampleSize());

                QuizQuestionContext context = buildTrainingContext(options);
                QuizQuestionSpec spec = buildSpec(
                                QuizQuestionSource.TRAINING,
                                sample.targetPersonId(),
                                sample.targetStorageKey(),
                                gameMode.getId(),
                                targetAttributeIds,
                                operator,
                                context,
                                sample,
                                timed,
                                timeLimitMs,
                                decision);

                return new PreparedEmit(decision.chosenFormat(), spec, ttlSeconds);
        }

        // ------------------------------------------------------------------
        // COURSE
        // ------------------------------------------------------------------
        @Transactional(readOnly = true)
        public PreparedEmit orchestrateCourse(
                        Long userId,
                        Long courseId,
                        Boolean timed,
                        Integer timeLimitMs,
                        Long ttlSeconds) {
                Objects.requireNonNull(userId, "userId");
                Objects.requireNonNull(courseId, "courseId");

                Course course = courseDao.findById(courseId)
                                .orElseThrow(() -> new IllegalArgumentException("Course not found: id=" + courseId));

                Long gameModeId = course.getGameMode() != null ? course.getGameMode().getId() : null;
                GameMode gameMode = gameModeService.findByIdOrThrow(gameModeId);

                List<Long> targetAttributeIds = extractTargetAttributeIds(gameMode);
                String operator = resolveOperator(gameMode);
                FollowFilter scope = toFollowFilter(course.getPopulationScope());

                SelectionResult selection = knowledgeSelectionService.findNextDueSingleTarget(
                                course,
                                null,
                                false,
                                null);

                Knowledge knowledge = selection == null ? null : selection.knowledge();
                Long targetPersonId = (knowledge != null && knowledge.getPerson() != null)
                                ? knowledge.getPerson().getId()
                                : null;

                if (targetPersonId == null) {
                        throw new QuizUnprocessableException(
                                        QuizUnprocessableException.ErrorCode.NO_CANDIDATE,
                                        "No course target available for courseId=" + courseId);
                }

                EligibilityStats stats = candidateAccessor.countEligibility(buildCandidateQuery(
                                userId,
                                scope,
                                null,
                                gameMode.getId(),
                                targetAttributeIds,
                                operator,
                                true,
                                false,
                                null));

                PlanningDecision decision = formatPlanner
                                .plan(PlanningRequest.auto(stats, gameMode, timed, timeLimitMs));

                CandidateSample sample = candidateAccessor.sampleWithTarget(buildCandidateQuery(
                                userId,
                                scope,
                                null,
                                gameMode.getId(),
                                targetAttributeIds,
                                operator,
                                false,
                                decision.requiresPhoto(),
                                decision.sampleSize()), targetPersonId, decision.sampleSize());

                QuizQuestionContext context = buildCourseContext(course, selection, knowledge);

                QuizQuestionSpec spec = buildSpec(
                                QuizQuestionSource.COURSE,
                                sample.targetPersonId(),
                                sample.targetStorageKey(),
                                gameMode.getId(),
                                targetAttributeIds,
                                operator,
                                context,
                                sample,
                                timed,
                                timeLimitMs,
                                decision);

                return new PreparedEmit(decision.chosenFormat(), spec, ttlSeconds);
        }

        private QuizQuestionSpec buildSpec(
                        QuizQuestionSource source,
                        Long personId,
                        String storageKey,
                        Long gameModeId,
                        List<Long> targetAttributeIds,
                        String operator,
                        QuizQuestionContext context,
                        CandidateSample sample,
                        Boolean timed,
                        Integer timeLimitMs,
                        PlanningDecision decision) {

                return new QuizQuestionSpec.Builder()
                                .withSource(source)
                                .withPersonId(personId)
                                .withStorageKey(storageKey)
                                .withGameModeId(gameModeId)
                                .withTargetAttributeIds(targetAttributeIds)
                                .withOperator(operator)
                                .withContext(context)

                                // Pool minimal: ids only
                                .withCandidatePoolPersonIds(sample != null ? sample.personIds() : List.of())
                                .withCandidatePoolItems(buildCandidatePoolItems(sample, targetAttributeIds))

                                .withTimed(timed)
                                .withTimeLimitMs(timeLimitMs)
                                .withReasonCode(decision.reasonCode())
                                .withReasonDetailsJson(decision.reasonDetailsJson())
                                .build();
        }

        private static List<QuizPayloadItem> buildCandidatePoolItems(
                        CandidateSample sample,
                        List<Long> targetAttributeIds) {

                if (sample == null || sample.items() == null || sample.items().isEmpty()) {
                        return List.of();
                }

                List<QuizPayloadItem> out = new ArrayList<>(sample.items().size());
                for (PayloadItem item : sample.items()) {
                        if (item == null || item.personId() == null) {
                                continue;
                        }

                        String label = resolveLabel(item, targetAttributeIds);
                        out.add(new QuizPayloadItem.Builder()
                                        .withPersonId(item.personId())
                                        .withStorageKey(item.photoStorageKey())
                                        .withLabel(label)
                                        .build());
                }

                return out;
        }

        private static String resolveLabel(PayloadItem item, List<Long> targetAttributeIds) {
                if (item == null || targetAttributeIds == null || targetAttributeIds.isEmpty()) {
                        return null;
                }

                for (Long attrId : targetAttributeIds) {
                        if (attrId == null) {
                                continue;
                        }
                        String value = item.attributeValue(attrId);
                        if (value != null && !value.isBlank()) {
                                return value.trim();
                        }
                }

                return null;
        }

        private CandidateQuery buildCandidateQuery(
                        Long userId,
                        FollowFilter scope,
                        CategorySelection category,
                        Long gameModeId,
                        List<Long> targetAttributeIds,
                        String operator,
                        boolean countOnly,
                        boolean requireApprovedPhoto,
                        Integer limit) {

                CandidateQuery.Builder builder = new CandidateQuery.Builder()
                                .withUserId(userId)
                                .withExcludePersonId(userId)
                                .withPopulationScope(scope)
                                .withGameModeId(gameModeId)
                                .withGameModeAttributeIds(targetAttributeIds)
                                .withAttributeOperator(operator)
                                .countOnly(countOnly)
                                .requireApprovedPhoto(requireApprovedPhoto)
                                .requireCategoryMatch(category != null);

                if (category != null) {
                        builder.withCategory(category.getAttributeId(), category.getValue());
                }
                if (limit != null) {
                        builder.withLimit(limit);
                }

                return builder.build();
        }

        private static List<Long> extractTargetAttributeIds(GameMode gameMode) {
                Objects.requireNonNull(gameMode, "gameMode");
                List<GameModeAttribute> attrs = gameMode.getGameModeAttributes();
                if (attrs == null || attrs.isEmpty()) {
                        throw new IllegalArgumentException("GameMode has no attributes: id=" + gameMode.getId());
                }

                List<Long> ids = attrs.stream()
                                .map(GameModeAttribute::getAttribute)
                                .filter(Objects::nonNull)
                                .map(a -> a.getId())
                                .filter(Objects::nonNull)
                                .distinct()
                                .toList();

                if (ids.isEmpty()) {
                        throw new IllegalArgumentException("GameMode has no attribute ids: id=" + gameMode.getId());
                }
                return ids;
        }

        private static String resolveOperator(GameMode gameMode) {
                String op = gameMode.getOperator();
                return (op == null || op.isBlank()) ? "AND" : op;
        }

        private static FollowFilter toFollowFilter(PopulationScope scope) {
                if (scope == null)
                        return FollowFilter.ALL;
                return switch (scope) {
                        case FOLLOWED -> FollowFilter.FOLLOWED;
                        case ALL -> FollowFilter.ALL;
                };
        }

        private QuizQuestionContext buildTrainingContext(TrainingOptions options) {
                return new QuizQuestionContext.Builder()
                                .withSource(com.saymyname.core.model.enums.quiz.QuizQuestionSource.TRAINING)
                                .withKnowledgeTracking(null)
                                .build();
        }

        private QuizQuestionContext buildCourseContext(Course course, SelectionResult selection, Knowledge knowledge) {
                Integer round = course.getCurrentRound();
                PoolType poolType = null;
                if (selection != null) {
                        poolType = selection.poolType();
                }

                Long knowledgeId = (knowledge != null) ? knowledge.getId() : null;

                return new QuizQuestionContext.Builder()
                                .withSource(com.saymyname.core.model.enums.quiz.QuizQuestionSource.COURSE)
                                .withCourseId(course.getId())
                                .withKnowledgeId(knowledgeId) // ✅ crucial pour put()
                                .withQuestionRound(round) // optionnel MVP
                                .withPoolType(poolType != null ? poolType : PoolType.NEW)
                                .build();
        }
}
