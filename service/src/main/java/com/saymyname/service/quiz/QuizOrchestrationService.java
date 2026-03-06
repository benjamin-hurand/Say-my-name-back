// src/main/java/com/saymyname/service/quiz/QuizOrchestrationService.java
package com.saymyname.service.quiz;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.exception.quiz.QuizUnprocessableException;
import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.CourseRecentStats;
import com.saymyname.core.model.course.KnowledgeCandidate;
import com.saymyname.core.model.enums.FollowFilter;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.enums.PoolType;
import com.saymyname.core.model.enums.PopulationScope;
import com.saymyname.core.model.enums.quiz.FormatMode;
import com.saymyname.core.model.enums.quiz.QuizQuestionSource;
import com.saymyname.core.model.quiz.QuizPayloadItem;
import com.saymyname.core.model.quiz.QuizQuestionContext;
import com.saymyname.core.model.quiz.QuizQuestionSpec;
import com.saymyname.core.model.quiz.candidate.CandidateQuery;
import com.saymyname.core.model.quiz.candidate.CandidateSample;
import com.saymyname.core.model.quiz.candidate.EligibilityStats;
import com.saymyname.core.model.quiz.candidate.PayloadItem;
import com.saymyname.core.model.quiz.options.CategorySelection;
import com.saymyname.core.model.quiz.options.TrainingOptions;
import com.saymyname.core.model.quiz.planning.PlanningDecision;
import com.saymyname.core.model.quiz.planning.PlanningRequest;
import com.saymyname.core.model.quiz.planning.PreparedEmit;
import com.saymyname.persistence.dao.course.CourseDao;
import com.saymyname.service.course.CourseRecentStatsService;
import com.saymyname.service.course.KnowledgeSelectionService;
import com.saymyname.service.course.KnowledgeSelectionService.SelectionResult;
import com.saymyname.service.quiz.candidate.CandidateAccessor;
import com.saymyname.service.quiz.planning.FormatPlanner;
import com.saymyname.service.tenant.TenantMembershipService;

@Service
public class QuizOrchestrationService {

        private final CourseDao courseDao;
        private final KnowledgeSelectionService knowledgeSelectionService;
        private final CandidateAccessor candidateAccessor;
        private final FormatPlanner formatPlanner;
        private final CourseRecentStatsService courseRecentStatsService;
        private final TenantMembershipService tenantMembershipService;

        // D3: Stress thresholds for timed gating
        private static final int STRESS_ERROR_STREAK_THRESHOLD = 2;
        private static final double STRESS_AVG_RT_THRESHOLD_MS = 8000.0;

        public QuizOrchestrationService(
                        CourseDao courseDao,
                        KnowledgeSelectionService knowledgeSelectionService,
                        CandidateAccessor candidateAccessor,
                        FormatPlanner formatPlanner,
                        CourseRecentStatsService courseRecentStatsService,
                        TenantMembershipService tenantMembershipService) {

                this.courseDao = Objects.requireNonNull(courseDao, "courseDao");
                this.knowledgeSelectionService = Objects.requireNonNull(knowledgeSelectionService,
                                "knowledgeSelectionService");
                this.candidateAccessor = Objects.requireNonNull(candidateAccessor, "candidateAccessor");
                this.formatPlanner = Objects.requireNonNull(formatPlanner, "formatPlanner");
                this.courseRecentStatsService = Objects.requireNonNull(courseRecentStatsService,
                                "courseRecentStatsService");
                this.tenantMembershipService = Objects.requireNonNull(tenantMembershipService,
                                "TenantMembershipService");
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
                Objects.requireNonNull(formatMode, "formatMode");

                FollowFilter scope = options.getPopulationScope();

                CandidateQuery countQuery = buildCandidateQuery(
                                userId,
                                scope,
                                options.getCategory(),
                                options.getTargetAttributeId(),
                                true,
                                false,
                                null);

                EligibilityStats stats = candidateAccessor.countEligibility(countQuery);

                PlanningRequest planningRequest = (formatMode == FormatMode.FORCED)
                                ? PlanningRequest.forced(forcedFormat, stats, options.getTargetAttributeId(), timed,
                                                timeLimitMs)
                                : PlanningRequest.auto(stats, options.getTargetAttributeId(), timed, timeLimitMs);

                PlanningDecision decision = formatPlanner.plan(planningRequest);

                CandidateQuery sampleQuery = buildCandidateQuery(
                                userId,
                                scope,
                                options.getCategory(),
                                options.getTargetAttributeId(),
                                false,
                                decision.requiresPhoto(),
                                decision.sampleSize());

                CandidateSample sample = candidateAccessor.sample(sampleQuery, decision.sampleSize());

                QuizQuestionContext context = buildTrainingContext(options);

                // Training: pas de gating D3 ici (c'est course-centric dans ton code).
                QuizQuestionSpec spec = buildSpec(
                                QuizQuestionSource.TRAINING,
                                sample.targetPersonId(),
                                sample.targetStorageKey(),
                                options.getTargetAttributeId(),
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

                FollowFilter scope = toFollowFilter(course.getPopulationScope());

                SelectionResult selection = knowledgeSelectionService.findNextDueSingleTarget(
                                course,
                                null,
                                false,
                                null);

                KnowledgeCandidate candidate = (selection != null) ? selection.candidate() : null;
                Long targetPersonId = (candidate != null) ? candidate.personId() : null;

                if (targetPersonId == null) {
                        throw new QuizUnprocessableException(
                                        QuizUnprocessableException.ErrorCode.NO_CANDIDATE,
                                        "No course target available for courseId=" + courseId);
                }

                EligibilityStats stats = candidateAccessor.countEligibility(buildCandidateQuery(
                                userId,
                                scope,
                                null,
                                course.getTargetAttributeId(),
                                true,
                                false,
                                null));

                KnowledgeStatus knowledgeStatus = (candidate != null) ? candidate.status() : null;

                // ✅ D3 gating
                Boolean effectiveTimed = applyTimedGating(courseId, timed);

                PlanningDecision decision = formatPlanner.plan(
                                PlanningRequest.courseAuto(
                                                stats,
                                                course.getTargetAttributeId(),
                                                knowledgeStatus,
                                                effectiveTimed,
                                                timeLimitMs));

                CandidateSample sample = candidateAccessor.sampleWithTarget(
                                buildCandidateQuery(
                                                userId,
                                                scope,
                                                null,
                                                course.getTargetAttributeId(),
                                                false,
                                                decision.requiresPhoto(),
                                                decision.sampleSize()),
                                targetPersonId,
                                decision.sampleSize());

                QuizQuestionContext context = buildCourseContext(course, selection, candidate);

                // ✅ IMPORTANT: on émet la question avec effectiveTimed (pas timed)
                QuizQuestionSpec spec = buildSpec(
                                QuizQuestionSource.COURSE,
                                sample.targetPersonId(),
                                sample.targetStorageKey(),
                                course.getTargetAttributeId(),
                                context,
                                sample,
                                effectiveTimed,
                                timeLimitMs,
                                decision);

                return new PreparedEmit(decision.chosenFormat(), spec, ttlSeconds);
        }

        private QuizQuestionSpec buildSpec(
                        QuizQuestionSource source,
                        Long personId,
                        String storageKey,
                        Long targetAttributeId,
                        QuizQuestionContext context,
                        CandidateSample sample,
                        Boolean timed,
                        Integer timeLimitMs,
                        PlanningDecision decision) {

                return new QuizQuestionSpec.Builder()
                                .withSource(source)
                                .withPersonId(personId)
                                .withStorageKey(storageKey)
                                .withTargetAttributeId(targetAttributeId)
                                .withContext(context)
                                .withCandidatePoolPersonIds(sample != null ? sample.personIds() : List.of())
                                .withCandidatePoolItems(buildCandidatePoolItems(sample, targetAttributeId))
                                .withTimed(timed)
                                .withTimeLimitMs(timeLimitMs)
                                .withReasonCode(decision.reasonCode())
                                .withReasonDetailsJson(decision.reasonDetailsJson())
                                .build();
        }

        private static List<QuizPayloadItem> buildCandidatePoolItems(
                        CandidateSample sample,
                        Long targetAttributeId) {

                if (sample == null || sample.items() == null || sample.items().isEmpty()) {
                        return List.of();
                }

                List<QuizPayloadItem> out = new ArrayList<>(sample.items().size());
                for (PayloadItem item : sample.items()) {
                        if (item == null || item.personId() == null) {
                                continue;
                        }

                        String label = resolveLabel(item, targetAttributeId);
                        out.add(new QuizPayloadItem.Builder()
                                        .withPersonId(item.personId())
                                        .withStorageKey(item.photoStorageKey())
                                        .withLabel(label)
                                        .build());
                }

                return out;
        }

        private static String resolveLabel(PayloadItem item, Long targetAttributeId) {
                if (item == null || targetAttributeId == null) {
                        return null;
                }

                String value = item.attributeValue(targetAttributeId);
                if (value != null && !value.isBlank()) {
                        return value.trim();
                }

                return null;
        }

        private CandidateQuery buildCandidateQuery(
                        Long userId,
                        FollowFilter scope,
                        CategorySelection category,
                        Long targetAttributeId,
                        boolean countOnly,
                        boolean requireApprovedPhoto,
                        Integer limit) {

                // NOTE: tu pourrais remonter excludePersonId depuis le controller/security
                // context
                // pour éviter l'appel à chaque fois. Pour l'instant on garde simple.
                Long excludePersonId = tenantMembershipService.findPersonIdByUserId(userId)
                                .orElseThrow(() -> new IllegalStateException("No personId for userId=" + userId));

                CandidateQuery.Builder builder = new CandidateQuery.Builder()
                                .withUserId(userId)
                                .withExcludePersonId(excludePersonId)
                                .withPopulationScope(scope)
                                .withAttributeId(targetAttributeId)
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

        private static FollowFilter toFollowFilter(PopulationScope scope) {
                if (scope == null) {
                        return FollowFilter.ALL;
                }
                return switch (scope) {
                        case FOLLOWED -> FollowFilter.FOLLOWED;
                        case ALL -> FollowFilter.ALL;
                        // Si ton enum PopulationScope a UNFOLLOWED, dé-commente :
                        // case UNFOLLOWED -> FollowFilter.UNFOLLOWED;
                };
        }

        private QuizQuestionContext buildTrainingContext(TrainingOptions options) {
                return new QuizQuestionContext.Builder()
                                .withSource(QuizQuestionSource.TRAINING)
                                .withKnowledgeTracking(null)
                                .build();
        }

        private QuizQuestionContext buildCourseContext(
                        Course course,
                        SelectionResult selection,
                        KnowledgeCandidate candidate) {

                Integer round = course.getCurrentRound();
                PoolType poolType = (selection != null) ? selection.poolType() : null;

                Long knowledgeId = (candidate != null) ? candidate.knowledgeId() : null;

                return new QuizQuestionContext.Builder()
                                .withSource(QuizQuestionSource.COURSE)
                                .withCourseId(course.getId())
                                .withKnowledgeId(knowledgeId)
                                .withQuestionRound(round)
                                .withPoolType(poolType != null ? poolType : PoolType.NEW)
                                .build();
        }

        /**
         * D3: Timed gating - block timed mode when user is stressed.
         * Stress is detected by high error streak or slow response times.
         */
        private Boolean applyTimedGating(Long courseId, Boolean requestedTimed) {
                if (!Boolean.TRUE.equals(requestedTimed)) {
                        return requestedTimed;
                }

                CourseRecentStats recentStats = courseRecentStatsService.getStatsForCourse(courseId);
                if (recentStats == null) {
                        return requestedTimed;
                }

                boolean isStressed = recentStats.getErrorStreak() >= STRESS_ERROR_STREAK_THRESHOLD
                                || recentStats.getAvgRtRecent() > STRESS_AVG_RT_THRESHOLD_MS;

                if (isStressed) {
                        return false;
                }

                return requestedTimed;
        }
}