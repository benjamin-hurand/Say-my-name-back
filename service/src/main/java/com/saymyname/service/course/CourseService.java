// src/main/java/com/saymyname/service/course/CourseService.java
package com.saymyname.service.course;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.saymyname.core.exception.course.CourseAlreadyExistsException;
import com.saymyname.core.exception.course.NextQuestionUnavailableException;
import com.saymyname.core.exception.course.QuestionAlreadyAnsweredException;
import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.CourseAnswerResult;
import com.saymyname.core.model.course.CourseQuestionHistory;
import com.saymyname.core.model.course.CourseQuestionItem;
import com.saymyname.core.model.course.CourseQuestionPlan;
import com.saymyname.core.model.course.CourseRecentStats;
import com.saymyname.core.model.course.CourseStats;
import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.course.KnowledgeResultEvent;
import com.saymyname.core.model.course.KnowledgeStats;
import com.saymyname.core.model.enums.CourseStatus;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.enums.PoolType;
import com.saymyname.core.model.enums.PopulationScope;
import com.saymyname.core.model.enums.course.QuizQuestionItemRole;
import com.saymyname.core.model.enums.quiz.QuizDecisionReasonCode;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.enums.quiz.QuizPreferredFormat;
import com.saymyname.core.model.quiz.QuizAnswerItemResult;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.QuizValidationResult;
import com.saymyname.core.model.quiz.snapshot.TruthAttributeValue;
import com.saymyname.persistence.dao.course.CourseDao;
import com.saymyname.service.UserService;
import com.saymyname.service.UserSubscriptionService;
import com.saymyname.service.person.PersonService;
import com.saymyname.service.quiz.QuizAnswerValidator;
import com.saymyname.service.quiz.QuizQuestionSnapshotFactory;
import com.saymyname.service.quiz.QuizQuestionSnapshotMapper;

@Service
public class CourseService {

    private final CourseDao courseDao;
    private final KnowledgeService knowledgeService;
    private final KnowledgeStatsService knowledgeStatsService;
    private final CourseRecentStatsService courseRecentStatsService;
    private final KnowledgeSelectionService knowledgeSelectionService;
    private final CourseQuestionHistoryService courseQuestionHistoryService;
    private final UserSubscriptionService userSubscriptionService;
    private final PersonService personService;
    private final UserService userService;

    private final CourseQuizQuestionBuilder courseQuizQuestionBuilder;
    private final CourseQuizPlanPolicy courseQuizPlanPolicy;

    /** Snapshot: freeze truth centralisé (training + course). */
    private final QuizQuestionSnapshotFactory snapshotFactory;

    /** Validation snapshot (plus de DB-live). */
    private final QuizAnswerValidator quizAnswerValidator;

    /** ObjectMapper injecté (config Spring/Jackson). */
    private final ObjectMapper objectMapper;

    private static final List<CourseStatus> ACTIVE_STATUSES = List.of(CourseStatus.IN_PROGRESS);

    private static final double WEIGHT_ERROR = 5;
    private static final double WEIGHT_SRS = 4;
    private static final double WEIGHT_NOT_SO_NEW = 6;
    private static final double WEIGHT_NEW = 3;
    private static final double WEIGHT_REVISION = 0;
    private static final int MULTI_TARGET_MIN = 4;
    private static final int MULTI_TARGET_MAX_ERROR_STREAK = 0;
    private static final int MULTI_TARGET_MAX_AVG_RT_MS = 9000;
    private static final double MULTI_TARGET_MAX_HELP_RECENT = 1.0;
    private static final double MULTI_TARGET_MIN_ATTEMPTS_RECENT = 0.0;
    private static final int MULTI_TARGET_FETCH_FACTOR = 3;

    public CourseService(
            CourseDao courseDao,
            KnowledgeService knowledgeService,
            KnowledgeStatsService knowledgeStatsService,
            CourseRecentStatsService courseRecentStatsService,
            KnowledgeSelectionService knowledgeSelectionService,
            CourseQuestionHistoryService courseQuestionHistoryService,
            UserSubscriptionService userSubscriptionService,
            PersonService personService,
            UserService userService,
            CourseQuizQuestionBuilder courseQuizQuestionBuilder,
            CourseQuizPlanPolicy courseQuizPlanPolicy,
            QuizQuestionSnapshotFactory snapshotFactory,
            QuizAnswerValidator quizAnswerValidator,
            ObjectMapper objectMapper) {

        this.courseDao = courseDao;
        this.knowledgeService = knowledgeService;
        this.knowledgeStatsService = knowledgeStatsService;
        this.courseRecentStatsService = courseRecentStatsService;
        this.knowledgeSelectionService = knowledgeSelectionService;
        this.courseQuestionHistoryService = courseQuestionHistoryService;
        this.userSubscriptionService = userSubscriptionService;
        this.personService = personService;
        this.userService = userService;
        this.courseQuizQuestionBuilder = courseQuizQuestionBuilder;
        this.courseQuizPlanPolicy = courseQuizPlanPolicy;

        this.snapshotFactory = snapshotFactory;
        this.quizAnswerValidator = quizAnswerValidator;
        this.objectMapper = objectMapper;
    }

    // ---------------------------------------------------------------------
    // Course lifecycle
    // ---------------------------------------------------------------------

    @Transactional(readOnly = true)
    public Optional<Course> getLastUsedCourse() {
        Long userId = userService.getCurrentIdOrThrow();

        Optional<Course> focused = courseDao.findLastAccessedFirstActive(userId, ACTIVE_STATUSES);
        if (focused.isPresent())
            return focused;

        var actives = courseDao.findAllByUserAndStatusesOrderedByUpdatedAt(userId, ACTIVE_STATUSES);
        if (!actives.isEmpty())
            return Optional.of(actives.get(0));

        return courseDao.getCurrentCourse(userId);
    }

    @Transactional
    public Course createCourse(Course proto) {
        User me = userService.getCurrentAuthenticatedUserOrThrow();
        proto.setUser(me);

        if (proto.getPopulationScope() == null)
            proto.setPopulationScope(PopulationScope.FOLLOWED);
        if (proto.getStatus() == null)
            proto.setStatus(CourseStatus.IN_PROGRESS);

        var existing = courseDao.findFirstByUserModeScopeAndStatus(
                me.getId(),
                proto.getGameMode().getId(),
                proto.getPopulationScope(),
                CourseStatus.IN_PROGRESS);

        if (existing.isPresent())
            throw new CourseAlreadyExistsException();

        Course created = courseDao.saveCourse(proto);
        knowledgeService.insertBatchOfTenKnowledges(created);
        return created;
    }

    @Transactional
    public Course createOrResume(Course proto) {
        User me = userService.getCurrentAuthenticatedUserOrThrow();
        proto.setUser(me);

        if (proto.getPopulationScope() == null)
            proto.setPopulationScope(PopulationScope.FOLLOWED);
        if (proto.getStatus() == null)
            proto.setStatus(CourseStatus.IN_PROGRESS);

        var existing = courseDao.findFirstByUserModeScopeAndStatus(
                me.getId(),
                proto.getGameMode().getId(),
                proto.getPopulationScope(),
                CourseStatus.IN_PROGRESS);

        if (existing.isPresent())
            return existing.get();

        Course created = courseDao.saveCourse(proto);
        knowledgeService.insertBatchOfTenKnowledges(created);
        return created;
    }

    @Transactional
    public Course restartCourse(Long courseId, long userId) {
        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        if (!course.getUser().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        courseQuestionHistoryService.deleteAllByCourse(course);

        final double BASELINE_EASE = 2.5;
        final double BASELINE_DIFF = 1.0;
        final double BASELINE_STAB = 1.0;
        var scope = course.getPopulationScope() != null ? course.getPopulationScope() : PopulationScope.FOLLOWED;

        knowledgeService.resetForCourseScope(
                userId,
                course.getGameMode().getId(),
                scope,
                BASELINE_EASE, BASELINE_DIFF, BASELINE_STAB);

        course.setStatus(CourseStatus.IN_PROGRESS);
        course.setCurrentRound(0);

        return courseDao.saveCourse(course);
    }

    @Transactional
    public void updateCourse(Course course) {
        courseDao.saveCourse(course);
    }

    public Course findById(Long courseId) {
        return courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
    }

    @Transactional
    public void touchLastAccessed(Long courseId) {
        courseDao.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        courseDao.touchLastAccessed(courseId, LocalDateTime.now());
    }

    // ---------------------------------------------------------------------
    // Continue / Emit
    // ---------------------------------------------------------------------

    /** Continue returns a QuizQuestion directly. */
    @Transactional
    public QuizQuestion continueCourse(
            Long courseId,
            QuizPreferredFormat preferredFormat,
            Boolean timed,
            Integer timeLimitMs) {
        CourseQuestionHistory nextHistory = continueCourseHistory(courseId, preferredFormat, timed, timeLimitMs);
        ensureSnapshotCourseQuestionId(nextHistory);
        return QuizQuestionSnapshotMapper.toQuestion(nextHistory.getSnapshot());
    }

    /**
     * Internal: persists the next history (used by continue + answer).
     *
     * Round management (perfect):
     * - currentRound increments ONLY if we successfully emit+persist a new
     * question.
     * - history.questionRound == nextRound by construction.
     *
     * Freeze truth at emission time and persist snapshot.
     */
    @Transactional
    protected CourseQuestionHistory continueCourseHistory(
            Long courseId,
            QuizPreferredFormat preferredFormat,
            Boolean timed,
            Integer timeLimitMs) {
        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        courseDao.touchLastAccessed(course.getId(), LocalDateTime.now());

        if (knowledgeService.countByCourseAndStatus(course, KnowledgeStatus.UNKNOWN) == 0) {
            knowledgeService.insertBatchOfTenKnowledges(course);
        }

        // 1) Pick next history FIRST (no side effect on course rounds yet)
        CourseQuestionHistory history = findNextDue(course, null, null, null, true);
        if (history == null)
            throw new NextQuestionUnavailableException();

        // 2) Determine nextRound from course state (single source of truth)
        int nextRound = course.getCurrentRound() + 1;

        // 3) Enforce questionRound
        history.setQuestionRound(nextRound);

        // 4) Compute plan + targets
        CourseQuestionPlan plan = buildPlan(
                course,
                null,
                history,
                preferredFormat,
                timed,
                timeLimitMs);
        history.setPlan(plan);

        // 5) Build runtime question (payload/display)
        QuizQuestion q = courseQuizQuestionBuilder.buildFromHistory(history, plan);

        // 6) Freeze truth + build snapshot
        List<TruthAttributeValue> frozen = snapshotFactory.freezeTruthForQuestion(q);
        history.setSnapshot(snapshotFactory.fromQuestion(q, frozen, targetPersonIds(history)));

        // 7) Persist history
        CourseQuestionHistory persisted = courseQuestionHistoryService.create(history);

        ensureSnapshotCourseQuestionId(persisted);

        // 7) Update course round ONLY after successful emission (same TX => atomic)
        course.setCurrentRound(nextRound);
        updateCourse(course);

        return persisted;
    }

    // ---------------------------------------------------------------------
    // Stats
    // ---------------------------------------------------------------------

    @Transactional(readOnly = true)
    public CourseStats getStats(Long courseId) {
        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        long unknown = knowledgeService.countByCourseAndStatus(course, KnowledgeStatus.UNKNOWN);
        long discovered = knowledgeService.countByCourseAndStatus(course, KnowledgeStatus.DISCOVERED);
        long learned = knowledgeService.countByCourseAndStatus(course, KnowledgeStatus.LEARNED);
        long mastered = knowledgeService.countByCourseAndStatus(course, KnowledgeStatus.MASTERED);

        long totalCandidates = userSubscriptionService.countFollowedEligibleForMode(course);
        long universeEligible = personService.countUniverseEligibleForMode(course);

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long totalAnswers = courseQuestionHistoryService.countAllAnswersByCourse(course);
        long answersToday = courseQuestionHistoryService.countAnswersSince(course, startOfDay);
        LocalDateTime lastActivity = courseQuestionHistoryService.findLastAnsweredAt(course);

        long dueNow = knowledgeService.countDueNow(course);

        return new CourseStats.Builder()
                .withCourseId(course.getId())
                .withGameModeId(course.getGameMode().getId())
                .withTotalCandidates(totalCandidates)
                .withUniverseEligible(universeEligible)
                .withUnknown(unknown)
                .withDiscovered(discovered)
                .withLearned(learned)
                .withMastered(mastered)
                .withTotalAnswers(totalAnswers)
                .withAnswersToday(answersToday)
                .withLastActivity(lastActivity)
                .withCurrentRound(course.getCurrentRound())
                .withDueNow(dueNow)
                .build();
    }

    @Transactional(readOnly = true)
    public List<Course> findAllByUser() {
        Long userId = userService.getCurrentIdOrThrow();

        var list = courseDao.findAllByUserAndStatusesOrderedByLastAccess(userId, ACTIVE_STATUSES);
        if (!list.isEmpty())
            return list;

        return courseDao.findAllByUserAndStatusesOrderedByUpdatedAt(userId, ACTIVE_STATUSES);
    }

    @Transactional(readOnly = true)
    public List<CourseStats> getStatsForUser() {
        return findAllByUser().stream()
                .map(c -> getStats(c.getId()))
                .toList();
    }

    // ---------------------------------------------------------------------
    // Pools
    // ---------------------------------------------------------------------

    @Transactional
    private CourseQuestionHistory findNextDue(
            Course course,
            Long lastPersonId,
            Boolean correct,
            String feedback,
            boolean allowRepeat) {

        Map<PoolType, Double> weights = new LinkedHashMap<>(Map.of(
                PoolType.ERROR_RECENT, WEIGHT_ERROR,
                PoolType.SRS_DUE, WEIGHT_SRS,
                PoolType.NEW, WEIGHT_NEW,
                PoolType.DISCOVERED, WEIGHT_NOT_SO_NEW,
                PoolType.REVISION, WEIGHT_REVISION));

        KnowledgeSelectionService.SelectionResult selection = knowledgeSelectionService.findNextDueSingleTarget(
                course,
                lastPersonId,
                allowRepeat,
                weights);

        if (selection == null || selection.knowledge() == null) {
            return null;
        }

        Knowledge k = selection.knowledge();
        CourseQuestionItem target = new CourseQuestionItem.Builder()
                .withPosition(0)
                .withRole(QuizQuestionItemRole.TARGET)
                .withKnowledge(k)
                .withPerson(k.getPerson())
                .withAnswered(false)
                .withCorrect(null)
                .withNormalizedAnswer(null)
                .build();

        // NOTE:
        // questionRound is enforced by the caller (continueCourseHistory / answer).
        // This value is a placeholder only.
        return new CourseQuestionHistory.Builder()
                .withCourse(course)
                .withQuestionRound(course.getCurrentRound() + 1)
                .withAskedAt(LocalDateTime.now())
                .withAnsweredAt(null)
                .withResponseTimeMs(0)
                .withRawSubmission(null)
                .withNormalizedSubmission(null)
                .withGlobalCorrect(false)
                .withPoolType(selection.poolType())
                .withHelpUsed(false)
                .withItems(List.of(target))
                .build();
    }

    private static String getFeedbackMessage(Boolean correct) {
        return Boolean.TRUE.equals(correct) ? "Correct !" : "Incorrect !";
    }

    private String serializeRawSubmission(QuizAnswerSubmission submission) {
        if (submission == null)
            return null;

        try {
            return objectMapper.writeValueAsString(submission);
        } catch (JsonProcessingException e) {
            if (submission.getUserAnswer() != null)
                return submission.getUserAnswer();
            if (submission.getSelectedChoiceId() != null)
                return "selectedChoiceId=" + submission.getSelectedChoiceId();
            if (submission.getSelectedChoiceIds() != null)
                return "selectedChoiceIds=" + submission.getSelectedChoiceIds();
            if (submission.getSwipeRight() != null)
                return "swipeRight=" + submission.getSwipeRight();
            if (submission.getOrderingIds() != null)
                return "orderingIds=" + submission.getOrderingIds();
            if (submission.getPairs() != null)
                return "pairs=" + submission.getPairs();
            if (submission.getTimeMs() != null)
                return "timeMs=" + submission.getTimeMs();
            return null;
        }
    }

    // ---------------------------------------------------------------------
    // Answer
    // ---------------------------------------------------------------------

    @Transactional
    public CourseAnswerResult answer(
            Course course,
            Long courseQuestionHistoryId,
            QuizPreferredFormat preferredFormat,
            Boolean timed,
            Integer timeLimitMs,
            QuizAnswerSubmission submission) {

        CourseQuestionHistory previous = courseQuestionHistoryService.findById(courseQuestionHistoryId);
        if (previous == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found");
        if (previous.getAnsweredAt() != null)
            throw new QuestionAlreadyAnsweredException(previous.getId());

        // Round coherence guard (fail fast if something drifted)
        // Expect: course.currentRound == previous.questionRound (because it was
        // incremented at emission time)
        if (previous.getQuestionRound() > 0 && course.getCurrentRound() != previous.getQuestionRound()) {
            throw new IllegalStateException(
                    "Round mismatch: course.currentRound=" + course.getCurrentRound()
                            + " but previous.questionRound=" + previous.getQuestionRound());
        }

        // 1) Ensure snapshot exists (legacy safety)
        if (previous.getSnapshot() == null) {
            CourseQuestionPlan plan = previous.getPlan();
            if (plan == null) {
                throw new IllegalStateException("CourseQuestionHistory.plan is required to rebuild a snapshot");
            }
            QuizQuestion previousQuestionRuntime = courseQuizQuestionBuilder.buildFromHistory(previous, plan);
            List<TruthAttributeValue> frozen = snapshotFactory.freezeTruthForQuestion(previousQuestionRuntime);
            previous.setSnapshot(
                    snapshotFactory.fromQuestion(previousQuestionRuntime, frozen, targetPersonIds(previous)));
        }

        // 3) Single pipeline: normalize + validate from snapshot (no DB-live)
        QuizAnswerValidator.Evaluation eval = quizAnswerValidator.evaluateFromSnapshot(previous.getSnapshot(),
                submission);
        var normalized = eval.normalized();
        QuizValidationResult validation = eval.validation();

        String normalizedAudit = (normalized == null) ? null : normalized.auditString();

        // 4) Persist answer metadata
        LocalDateTime now = LocalDateTime.now();
        int deltaMs = (int) Math.max(0, ChronoUnit.MILLIS.between(previous.getAskedAt(), now));

        previous.setAnsweredAt(now);
        previous.setResponseTimeMs(deltaMs);
        previous.setRawSubmission(serializeRawSubmission(submission));
        previous.setNormalizedSubmission(normalizedAudit);

        List<QuizAnswerItemResult> itemResults = new ArrayList<>();
        List<KnowledgeResultEvent> srsEvents = new ArrayList<>();

        boolean allTargetsCorrect = true;

        for (CourseQuestionItem item : previous.getItems()) {
            if (item.getRole() != QuizQuestionItemRole.TARGET)
                continue;

            Long personId = item.getKnowledge().getPerson().getId();

            boolean correct = validation.isCorrect();
            if (!correct)
                allTargetsCorrect = false;

            item.setAnswered(true);
            item.setCorrect(correct);
            item.setNormalizedAnswer(normalizedAudit);

            itemResults.add(new QuizAnswerItemResult.Builder()
                    .withPosition(item.getPosition())
                    .withRole(item.getRole()) // même enum
                    .withKnowledgeId(item.getKnowledge().getId())
                    .withPersonId(personId)
                    .withCorrect(correct)
                    .withUserAnswerNormalized(normalizedAudit)
                    .withCorrectAnswer(validation.getCorrectAnswerDisplay())
                    .withResultAttributes(validation.getResultAttributes())
                    .build());

            srsEvents.add(new KnowledgeResultEvent.Builder()
                    .withKnowledgeId(item.getKnowledge().getId())
                    .withGameModeId(course.getGameMode().getId())
                    .withPersonId(personId)
                    .withCorrect(correct)
                    .withHelpUsed(previous.isHelpUsed())
                    .withCourseId(course.getId())
                    .withCourseQuestionHistoryId(previous.getId())
                    .withQuestionRound(previous.getQuestionRound())
                    .build());
        }

        previous.setGlobalCorrect(allTargetsCorrect);
        courseQuestionHistoryService.updateAnswerMetaAndItems(previous);

        if (!srsEvents.isEmpty()) {
            knowledgeService.recordBatchResults(course.getUser(), srsEvents);
        }

        updateRecentStatsAfterAnswer(course, previous);

        // 5) Prepare next question history (no round mutation yet)
        Long lastPersonId = lastTargetPersonId(previous);

        CourseQuestionHistory nextHistory = findNextDue(
                course,
                lastPersonId,
                allTargetsCorrect,
                getFeedbackMessage(allTargetsCorrect),
                false);

        if (nextHistory == null)
            throw new NextQuestionUnavailableException();

        // 6) Compute nextRound from previous round (most robust)
        int baseRound = previous.getQuestionRound() > 0 ? previous.getQuestionRound() : course.getCurrentRound();
        int nextRound = baseRound + 1;

        // enforce questionRound
        nextHistory.setQuestionRound(nextRound);

        // 7) Build next question + freeze truth + snapshot BEFORE persist
        CourseQuestionPlan nextPlan = buildPlan(
                course,
                previous,
                nextHistory,
                preferredFormat,
                timed,
                timeLimitMs);
        nextHistory.setPlan(nextPlan);

        QuizQuestion nextQuizQuestionRuntime = courseQuizQuestionBuilder.buildFromHistory(
                nextHistory,
                nextPlan);

        List<TruthAttributeValue> frozenNext = snapshotFactory.freezeTruthForQuestion(nextQuizQuestionRuntime);
        nextHistory.setSnapshot(snapshotFactory.fromQuestion(nextQuizQuestionRuntime, frozenNext,
                targetPersonIds(nextHistory)));

        // 8) Persist next history
        courseQuestionHistoryService.create(nextHistory);
        ensureSnapshotCourseQuestionId(nextHistory);

        // 9) Update course round ONLY after successful emission (same TX => atomic)
        course.setCurrentRound(nextRound);
        updateCourse(course);

        QuizQuestion nextQuestionFromSnapshot = QuizQuestionSnapshotMapper.toQuestion(nextHistory.getSnapshot());

        return new CourseAnswerResult.Builder()
                .withCorrect(allTargetsCorrect)
                .withRawSubmission(previous.getRawSubmission())
                .withNormalizedSubmission(previous.getNormalizedSubmission())
                .withFeedbackMessage(getFeedbackMessage(allTargetsCorrect))
                .withNextQuestion(nextQuestionFromSnapshot)
                .withItemResults(itemResults)
                .build();
    }

    private void ensureSnapshotCourseQuestionId(CourseQuestionHistory history) {
        if (history == null || history.getId() == null || history.getSnapshot() == null) {
            return;
        }
        if (history.getSnapshot().getContext() == null) {
            return;
        }
        if (history.getSnapshot().getContext().getCourseQuestionId() == null) {
            history.getSnapshot().getContext().setCourseQuestionId(history.getId());
        }
    }

    // ---------------------------------------------------------------------
    // helpers (targets)
    // ---------------------------------------------------------------------

    private void updateRecentStatsAfterAnswer(Course course, CourseQuestionHistory history) {
        if (course == null || history == null) {
            return;
        }

        LocalDateTime answeredAt = history.getAnsweredAt();
        int responseTimeMs = Math.max(0, history.getResponseTimeMs());
        boolean helpUsed = history.isHelpUsed();
        boolean globalCorrect = history.isGlobalCorrect();

        QuizFormat format = null;
        if (history.getPlan() != null) {
            format = history.getPlan().getFormat();
        } else if (history.getSnapshot() != null) {
            format = history.getSnapshot().getFormat();
        }

        courseRecentStatsService.upsertOnAnswer(
                course.getId(),
                format,
                globalCorrect,
                helpUsed,
                responseTimeMs,
                answeredAt);

        Long userId = course.getUser() != null ? course.getUser().getId() : null;
        Long gameModeId = course.getGameMode() != null ? course.getGameMode().getId() : null;
        if (userId == null || gameModeId == null) {
            return;
        }

        List<CourseQuestionItem> items = history.getItems();
        if (items == null || items.isEmpty()) {
            return;
        }

        for (CourseQuestionItem item : items) {
            if (item.getRole() != QuizQuestionItemRole.TARGET) {
                continue;
            }
            Knowledge knowledge = item.getKnowledge();
            Long knowledgeId = knowledge != null ? knowledge.getId() : null;
            Long personId = item.getPerson() != null ? item.getPerson().getId()
                    : (knowledge != null && knowledge.getPerson() != null ? knowledge.getPerson().getId() : null);

            if (knowledgeId == null || personId == null) {
                continue;
            }

            knowledgeStatsService.upsertOnAnswer(
                    userId,
                    gameModeId,
                    knowledgeId,
                    personId,
                    Boolean.TRUE.equals(item.getCorrect()),
                    helpUsed,
                    responseTimeMs,
                    answeredAt);
        }
    }

    private CourseQuestionPlan buildPlan(
            Course course,
            CourseQuestionHistory previousHistory,
            CourseQuestionHistory nextHistory,
            QuizPreferredFormat preferredFormat,
            Boolean timed,
            Integer timeLimitMs) {
        Knowledge knowledge = targetKnowledge(nextHistory);
        Long lastPersonId = previousHistory != null ? lastTargetPersonId(previousHistory) : null;
        List<Knowledge> extraTargets = findMultiTargetCandidates(course, knowledge, lastPersonId,
                MULTI_TARGET_MIN - 1);
        boolean multiTargetAvailable = extraTargets.size() >= Math.max(0, MULTI_TARGET_MIN - 1);

        CourseRecentStats courseStats = courseRecentStatsService.getStatsForCourse(course.getId());

        List<Long> statsKnowledgeIds = new ArrayList<>();
        if (knowledge != null && knowledge.getId() != null) {
            statsKnowledgeIds.add(knowledge.getId());
        }
        for (Knowledge extra : extraTargets) {
            if (extra != null && extra.getId() != null) {
                statsKnowledgeIds.add(extra.getId());
            }
        }

        Map<Long, KnowledgeStats> statsByKnowledgeId = knowledgeStatsService.getStatsForKnowledgeIds(
                course.getUser() != null ? course.getUser().getId() : null,
                course.getGameMode() != null ? course.getGameMode().getId() : null,
                statsKnowledgeIds);

        KnowledgeStats primaryStats = knowledge != null ? statsByKnowledgeId.get(knowledge.getId()) : null;
        List<KnowledgeStats> extraStats = extraTargets.stream()
                .map(k -> k != null ? statsByKnowledgeId.get(k.getId()) : null)
                .toList();

        CourseQuizPlanPolicy.Plan decision = courseQuizPlanPolicy.decide(
                course,
                previousHistory,
                knowledge,
                primaryStats,
                extraStats,
                courseStats,
                preferredFormat,
                timed,
                timeLimitMs,
                multiTargetAvailable);

        int needed = decision.targetCount() - 1;
        if (needed > 0) {
            if (extraTargets.size() < needed) {
                decision = fallbackToMcq(decision, QuizDecisionReasonCode.FALLBACK_MULTI_TARGET_SHORTFALL,
                        shortfallDetailsJson(decision, needed, extraTargets.size()));
            } else {
                applyAdditionalTargets(nextHistory, extraTargets.subList(0, needed));
            }
        }

        List<Long> targetKnowledgeIds = targetKnowledgeIds(nextHistory);
        if (targetKnowledgeIds.size() != decision.targetCount()) {
            throw new IllegalStateException("Plan targetCount mismatch: expected=" + decision.targetCount()
                    + " actual=" + targetKnowledgeIds.size()
                    + " reasonCode=" + decision.reasonCode().name());
        }

        return new CourseQuestionPlan.Builder()
                .withFormat(decision.format())
                .withTimed(decision.timed())
                .withTimeLimitMs(decision.timeLimitMs())
                .withTargetCount(decision.targetCount())
                .withTargetKnowledgeIds(targetKnowledgeIds)
                .withParamsJson(decision.paramsJson())
                .withReasonCode(decision.reasonCode())
                .withReasonDetailsJson(decision.reasonDetailsJson())
                .build();
    }

    private CourseQuizPlanPolicy.Plan fallbackToMcq(
            CourseQuizPlanPolicy.Plan base,
            QuizDecisionReasonCode reasonCode,
            String reasonDetailsJson) {
        return new CourseQuizPlanPolicy.Plan(
                QuizFormat.MCQ,
                base.timed(),
                base.timeLimitMs(),
                1,
                "{\"nbChoices\":4}",
                reasonCode,
                reasonDetailsJson);
    }

    private static String shortfallDetailsJson(
            CourseQuizPlanPolicy.Plan base,
            int neededExtras,
            int availableExtras) {
        StringBuilder sb = new StringBuilder(128);
        sb.append('{');
        sb.append("\"base_reason_code\":\"").append(base.reasonCode().name()).append('"');
        sb.append(",\"requested_target_count\":").append(base.targetCount());
        sb.append(",\"needed_extras\":").append(neededExtras);
        sb.append(",\"available_extras\":").append(availableExtras);
        sb.append('}');
        return sb.toString();
    }

    private List<Knowledge> findMultiTargetCandidates(
            Course course,
            Knowledge primary,
            Long lastPersonId,
            int limit) {
        if (course == null || limit <= 0) {
            return List.of();
        }
        KnowledgeSelectionService.MultiTargetConstraints constraints = new KnowledgeSelectionService.MultiTargetConstraints(
                MULTI_TARGET_MAX_ERROR_STREAK,
                MULTI_TARGET_MAX_AVG_RT_MS,
                MULTI_TARGET_MAX_HELP_RECENT,
                MULTI_TARGET_MIN_ATTEMPTS_RECENT,
                MULTI_TARGET_FETCH_FACTOR);

        return knowledgeSelectionService.findNextDueMultiTargets(
                course,
                primary,
                limit,
                lastPersonId,
                constraints);
    }

    private void applyAdditionalTargets(CourseQuestionHistory history, List<Knowledge> extraTargets) {
        if (history == null || extraTargets == null || extraTargets.isEmpty()) {
            return;
        }
        List<CourseQuestionItem> items = new ArrayList<>(
                history.getItems() != null ? history.getItems() : List.of());
        int pos = items.stream().mapToInt(CourseQuestionItem::getPosition).max().orElse(-1) + 1;
        for (Knowledge k : extraTargets) {
            if (k == null || k.getPerson() == null) {
                continue;
            }
            items.add(new CourseQuestionItem.Builder()
                    .withPosition(pos++)
                    .withRole(QuizQuestionItemRole.TARGET)
                    .withKnowledge(k)
                    .withPerson(k.getPerson())
                    .withAnswered(false)
                    .withCorrect(null)
                    .withNormalizedAnswer(null)
                    .build());
        }
        history.setItems(items);
    }

    private Knowledge targetKnowledge(CourseQuestionHistory h) {
        if (h == null || h.getItems() == null) {
            return null;
        }
        return h.getItems().stream()
                .filter(it -> it.getRole() == QuizQuestionItemRole.TARGET)
                .map(CourseQuestionItem::getKnowledge)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private List<CourseQuestionItem> targetItems(CourseQuestionHistory h) {
        if (h == null || h.getItems() == null)
            return List.of();
        return h.getItems().stream()
                .filter(it -> it.getRole() == QuizQuestionItemRole.TARGET)
                .toList();
    }

    private List<Long> targetKnowledgeIds(CourseQuestionHistory h) {
        return targetItems(h).stream()
                .map(it -> it.getKnowledge() != null ? it.getKnowledge().getId() : null)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<Long> targetPersonIds(CourseQuestionHistory h) {
        return targetItems(h).stream()
                .map(it -> it.getPerson() != null ? it.getPerson().getId() : null)
                .filter(Objects::nonNull)
                .toList();
    }

    private Long lastTargetPersonId(CourseQuestionHistory h) {
        return targetItems(h).stream()
                .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                .findFirst()
                .map(it -> it.getKnowledge().getPerson().getId())
                .orElse(null);
    }
}
