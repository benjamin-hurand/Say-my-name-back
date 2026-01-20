// src/main/java/com/saymyname/service/course/CourseService.java
package com.saymyname.service.course;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.exception.course.CourseAlreadyExistsException;
import com.saymyname.core.exception.course.NextQuestionUnavailableException;
import com.saymyname.core.exception.course.QuestionAlreadyAnsweredException;
import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.CourseQuestionAttempt;
import com.saymyname.core.model.course.CourseQuestionItem;
import com.saymyname.core.model.course.CourseQuestionPlan;
import com.saymyname.core.model.course.CourseRecentStats;
import com.saymyname.core.model.course.CourseStats;
import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.course.KnowledgeResultEvent;
import com.saymyname.core.model.course.KnowledgeStats;
import com.saymyname.core.model.course.RecentAnswerStat;
import com.saymyname.core.model.enums.CourseStatus;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.enums.PoolType;
import com.saymyname.core.model.enums.PopulationScope;
import com.saymyname.core.model.enums.course.QuizQuestionItemRole;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.enums.quiz.QuizPreferredFormat;
import com.saymyname.core.model.leaderboard.XpAward;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.quiz.QuizAnswerItemResult;
import com.saymyname.core.model.quiz.QuizAnswerResult;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizValidationResult;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.core.model.quiz.snapshot.TruthAttributeValue;
import com.saymyname.persistence.dao.course.CourseDao;
import com.saymyname.service.UserService;
import com.saymyname.service.UserSubscriptionService;
import com.saymyname.service.course.store.CourseAttemptStore;
import com.saymyname.service.person.PersonService;
import com.saymyname.core.model.enums.quiz.QuizQuestionSource;
import com.saymyname.core.model.quiz.QuizEvaluationResult;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.planning.PlanningContext;
import com.saymyname.core.model.quiz.planning.PlanningRequest;
import com.saymyname.core.model.quiz.planning.QuestionPlan;
import com.saymyname.core.model.quiz.planning.SessionStats;
import com.saymyname.service.quiz.QuizAnswerValidator;
import com.saymyname.service.quiz.QuizQuestionSnapshotFactory;
import com.saymyname.service.quiz.QuizQuestionSnapshotMapper;
import com.saymyname.service.quiz.CourseOptionsResolver;
import com.saymyname.service.quiz.handle.AttemptRef;
import com.saymyname.service.quiz.handle.QuizHandleCodec;
import com.saymyname.service.quiz.planning.QuestionPlanningService;
import com.saymyname.service.quiz.store.QuizAttemptStore.AttemptHandle;

@Service
public class CourseService {

    private static final Logger log = LoggerFactory.getLogger(CourseService.class);

    private final CourseDao courseDao;
    private final KnowledgeService knowledgeService;
    private final KnowledgeStatsService knowledgeStatsService;
    private final CourseRecentStatsService courseRecentStatsService;
    private final KnowledgeSelectionService knowledgeSelectionService;
    private final CourseQuizPlanPolicy courseQuizPlanPolicy;
    private final QuestionPlanningService questionPlanningService;
    private final CourseOptionsResolver courseOptionsResolver;
    private final CourseAttemptStore courseAttemptStore;
    private final UserService userService;

    private final CourseQuizQuestionBuilder courseQuizQuestionBuilder;
    private final QuizQuestionSnapshotFactory snapshotFactory;
    private final QuizHandleCodec quizHandleCodec;
    private final QuizAnswerValidator quizAnswerValidator;

    private final CoursePlanMode coursePlanMode;

    private static final List<CourseStatus> ACTIVE_STATUSES = List.of(CourseStatus.IN_PROGRESS);

    private static final double WEIGHT_ERROR = 5;
    private static final double WEIGHT_SRS = 4;
    private static final double WEIGHT_NOT_SO_NEW = 6;
    private static final double WEIGHT_NEW = 3;
    private static final double WEIGHT_REVISION = 0;

    private static final int MULTI_TARGET_MIN = 4;
    private static final int MULTI_TARGET_FETCH_FACTOR = 3;
    private static final int MULTI_TARGET_MAX_ERROR_STREAK = 0;
    private static final double MULTI_TARGET_MAX_AVG_RT_MS = 9000;
    private static final double MULTI_TARGET_MAX_HELP_RECENT = 1.0;
    private static final double MULTI_TARGET_MIN_ATTEMPTS_RECENT = 1.0;
    private static final int SESSION_STATS_LIMIT = 20;

    public CourseService(
            CourseDao courseDao,
            KnowledgeService knowledgeService,
            KnowledgeStatsService knowledgeStatsService,
            CourseRecentStatsService courseRecentStatsService,
            KnowledgeSelectionService knowledgeSelectionService,
            CourseAttemptStore courseAttemptStore,
            UserSubscriptionService userSubscriptionService,
            PersonService personService,
            UserService userService,
            CourseQuizQuestionBuilder courseQuizQuestionBuilder,
            CourseQuizPlanPolicy courseQuizPlanPolicy,
            QuestionPlanningService questionPlanningService,
            CourseOptionsResolver courseOptionsResolver,
            QuizQuestionSnapshotFactory snapshotFactory,
            QuizHandleCodec quizHandleCodec,
            QuizAnswerValidator quizAnswerValidator,
            @Value("${quiz.planning.course.mode:NEW}") String coursePlanningMode) {

        this.courseDao = courseDao;
        this.knowledgeService = knowledgeService;
        this.knowledgeStatsService = knowledgeStatsService;
        this.courseRecentStatsService = courseRecentStatsService;
        this.knowledgeSelectionService = knowledgeSelectionService;
        this.courseQuizPlanPolicy = courseQuizPlanPolicy;
        this.questionPlanningService = questionPlanningService;
        this.courseOptionsResolver = courseOptionsResolver;
        this.courseAttemptStore = courseAttemptStore;
        this.userService = userService;

        this.courseQuizQuestionBuilder = courseQuizQuestionBuilder;
        this.snapshotFactory = snapshotFactory;
        this.quizHandleCodec = quizHandleCodec;
        this.quizAnswerValidator = quizAnswerValidator;
        this.coursePlanMode = CoursePlanMode.from(coursePlanningMode);
    }

    // ---------------------------------------------------------------------
    // Course lifecycle (unchanged)
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

        courseAttemptStore.deleteAllByCourse(course);

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

    // ---------------------------------------------------------------------
    // Public API for CourseRestController
    // ---------------------------------------------------------------------

    /**
     * Continue course: emit next question with handle.
     * Called by CourseRestController.
     */
    @Transactional
    public QuizQuestion continueCourse(Long courseId) {
        Long userId = userService.getCurrentIdOrThrow();
        CourseQuestionAttempt attempt = emitNextCourseAttempt(courseId, userId);

        if (attempt == null || attempt.getId() == null || attempt.getSnapshot() == null) {
            throw new IllegalStateException("Course attempt emission failed (missing id/snapshot)");
        }

        QuizQuestion q = QuizQuestionSnapshotMapper.toQuestion(attempt.getSnapshot());

        String handle = quizHandleCodec.encode(new AttemptRef(
                QuizQuestionSource.COURSE,
                new AttemptHandle.DbIdHandle(attempt.getId())));

        q.setQuestionHandle(handle);
        return q;
    }

    /**
     * Answer course question.
     * Handles evaluation, multi-step state, and finalization directly.
     */
    @Transactional
    public QuizAnswerResult answer(Course course, String questionHandle, QuizAnswerSubmission submission) {
        Long userId = userService.getCurrentIdOrThrow();

        // Decode handle to get attempt ID
        AttemptRef ref = quizHandleCodec.decodeOrThrow(questionHandle);
        if (ref.source() != QuizQuestionSource.COURSE) {
            throw new IllegalArgumentException("Expected COURSE handle but got " + ref.source());
        }
        if (!(ref.handle() instanceof AttemptHandle.DbIdHandle dh)) {
            throw new IllegalArgumentException("COURSE requires DbIdHandle");
        }

        Long attemptId = dh.value();

        // Load attempt
        CourseQuestionAttempt attempt = courseAttemptStore.findById(attemptId);
        if (attempt == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found");
        }
        if (attempt.getAnsweredAt() != null) {
            throw new QuestionAlreadyAnsweredException(attempt.getId());
        }

        // Evaluate using QuizAnswerValidator directly
        QuizQuestionSnapshot snapshot = attempt.getSnapshot();
        QuizEvaluationResult eval = quizAnswerValidator.evaluate(snapshot, submission);

        // Multi-step incomplete: update state and return
        if (eval.isMultiStepIncomplete()) {
            QuizQuestionSnapshot updatedSnapshot = eval.updatedSnapshot();
            String normalizedAudit = eval.normalizedAudit() != null ? eval.normalizedAudit().auditString() : null;

            attempt.setSnapshot(updatedSnapshot);
            attempt.setNormalizedAudit(normalizedAudit);
            courseAttemptStore.updateSnapshot(attemptId, userId, updatedSnapshot);

            QuizQuestion currentQuestion = QuizQuestionSnapshotMapper.toQuestion(updatedSnapshot);
            currentQuestion.setQuestionHandle(questionHandle); // keep same handle

            return new QuizAnswerResult.Builder()
                    .withCorrect(eval.correct())
                    .withFeedbackMessage(eval.feedbackMessage())
                    .withIsComplete(false)
                    .withCurrentState(eval.updatedState())
                    .withNextQuestion(currentQuestion)
                    .withItemResults(List.of())
                    .build();
        }

        // Complete: finalize and emit next
        QuizQuestionSnapshot effectiveSnapshot = eval.updatedSnapshot() != null ? eval.updatedSnapshot() : snapshot;
        String normalizedAudit = eval.normalizedAudit() != null ? eval.normalizedAudit().auditString() : null;

        // Build QuizValidationResult for finalization (backward compat)
        QuizValidationResult validation = new QuizValidationResult.Builder()
                .withCorrect(eval.correct())
                .withIsComplete(eval.isComplete())
                .withUpdatedState(eval.updatedState())
                .build();

        return finalizeCourseAnswerAndMaybeEmitNext(
                course.getId(),
                attemptId,
                userId,
                effectiveSnapshot,
                submission,
                normalizedAudit,
                eval.feedbackMessage(),
                validation);
    }

    /**
     * List all courses for current user.
     */
    @Transactional(readOnly = true)
    public List<Course> findAllByUser() {
        Long userId = userService.getCurrentIdOrThrow();
        return courseDao.findAllByUserAndStatusesOrderedByLastAccess(userId, ACTIVE_STATUSES);
    }

    /**
     * Touch last accessed timestamp for a course.
     */
    @Transactional
    public void touchLastAccessed(Long courseId) {
        courseDao.touchLastAccessed(courseId, LocalDateTime.now());
    }

    /**
     * Get stats for all courses of current user.
     */
    @Transactional(readOnly = true)
    public List<CourseStats> getStatsForUser() {
        Long userId = userService.getCurrentIdOrThrow();
        List<Course> courses = courseDao.findAllByUserAndStatusesOrderedByLastAccess(userId,
                List.of(CourseStatus.IN_PROGRESS, CourseStatus.ARCHIVED));
        return courses.stream()
                .map(this::buildStatsForCourse)
                .toList();
    }

    private CourseStats buildStatsForCourse(Course course) {
        int totalAnswers = courseAttemptStore.countAllAnswersByCourse(course);
        LocalDateTime lastActivity = courseAttemptStore.findLastAnsweredAt(course);

        return new CourseStats.Builder()
                .withCourseId(course.getId())
                .withTotalAnswers(totalAnswers)
                .withLastActivity(lastActivity)
                .build();
    }

    // ---------------------------------------------------------------------
    // Called by QuizEngine - emit next course attempt (persisted, with snapshot)
    // ---------------------------------------------------------------------

    @Transactional
    public CourseQuestionAttempt emitNextCourseAttempt(Long courseId, Long userId) {
        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        if (course.getUser() == null || !Objects.equals(course.getUser().getId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        courseDao.touchLastAccessed(course.getId(), LocalDateTime.now());

        if (knowledgeService.countByCourseAndStatus(course, KnowledgeStatus.UNKNOWN) == 0) {
            knowledgeService.insertBatchOfTenKnowledges(course);
        }

        CourseQuestionAttempt attempt = findNextDue(course, null, null, true);
        if (attempt == null)
            throw new NextQuestionUnavailableException();

        int nextRound = course.getCurrentRound() + 1;
        attempt.setQuestionRound(nextRound);

        CourseQuestionPlan plan = buildPlan(course, null, attempt, null, null, null);
        attempt.setPlan(plan);

        // Build runtime question (course-specific builder), then snapshot-freeze
        var runtimeQuestion = courseQuizQuestionBuilder.buildFromAttempt(attempt, plan);

        List<TruthAttributeValue> frozen = snapshotFactory.freezeTruthForQuestion(runtimeQuestion);
        attempt.setSnapshot(snapshotFactory.fromQuestion(runtimeQuestion, frozen, targetPersonIds(attempt)));

        CourseQuestionAttempt persisted = courseAttemptStore.create(attempt);
        ensureSnapshotCourseQuestionId(persisted);

        course.setCurrentRound(nextRound);
        updateCourse(course);

        return persisted;
    }

    // ---------------------------------------------------------------------
    // Called by QuizEngine - finalize course answer + emit next
    // (QuizEngine already validated and handled multi-step completion)
    // ---------------------------------------------------------------------

    @Transactional
    public QuizAnswerResult finalizeCourseAnswerAndMaybeEmitNext(
            Long courseId,
            Long attemptId,
            Long userId,
            QuizQuestionSnapshot effectiveSnapshot,
            QuizAnswerSubmission submission,
            String normalizedAudit,
            String feedbackMessage,
            QuizValidationResult validation) {

        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        if (course.getUser() == null || !Objects.equals(course.getUser().getId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        CourseQuestionAttempt previous = courseAttemptStore.findById(attemptId);
        if (previous == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Question not found");
        }
        if (previous.getAnsweredAt() != null) {
            throw new QuestionAlreadyAnsweredException(previous.getId());
        }

        if (previous.getQuestionRound() > 0 && course.getCurrentRound() != previous.getQuestionRound()) {
            throw new IllegalStateException(
                    "Round mismatch: course.currentRound=" + course.getCurrentRound()
                            + " but previous.questionRound=" + previous.getQuestionRound());
        }

        // close question
        LocalDateTime now = LocalDateTime.now();
        int deltaMs = (int) Math.max(0, ChronoUnit.MILLIS.between(previous.getAskedAt(), now));

        previous.setAnsweredAt(now);
        previous.setResponseTimeMs(deltaMs);

        // rawSubmission is optional here; if you want, you can store JSON with
        // ObjectMapper in another service.
        previous.setRawSubmission(submission != null ? submission.getUserAnswer() : null);
        previous.setNormalizedAudit(normalizedAudit);

        if (effectiveSnapshot != null) {
            previous.setSnapshot(effectiveSnapshot);
        }

        List<QuizAnswerItemResult> itemResults = new ArrayList<>();
        List<KnowledgeResultEvent> srsEvents = new ArrayList<>();

        boolean correctFinal = validation != null && validation.isCorrect();
        boolean allTargetsCorrect = correctFinal;

        for (CourseQuestionItem item : previous.getItems()) {
            if (item.getRole() != QuizQuestionItemRole.TARGET) {
                continue;
            }

            Long personId = item.getKnowledge().getPerson().getId();

            item.setAnswered(true);
            item.setCorrect(correctFinal);
            item.setNormalizedAnswer(normalizedAudit);

            itemResults.add(new QuizAnswerItemResult.Builder()
                    .withPosition(item.getPosition())
                    .withRole(item.getRole())
                    .withKnowledgeId(item.getKnowledge().getId())
                    .withPersonId(personId)
                    .withCorrect(correctFinal)
                    .withUserAnswerNormalized(normalizedAudit)
                    .withCorrectAnswer(validation != null ? validation.getCorrectAnswerDisplay() : null)
                    .withResultAttributes(validation != null ? validation.getResultAttributes() : null)
                    .build());

            srsEvents.add(new KnowledgeResultEvent.Builder()
                    .withKnowledgeId(item.getKnowledge().getId())
                    .withGameModeId(course.getGameMode().getId())
                    .withPersonId(personId)
                    .withCorrect(correctFinal)
                    .withHelpUsed(previous.isHelpUsed())
                    .withCourseId(course.getId())
                    .withCourseQuestionAttemptId(previous.getId())
                    .withQuestionRound(previous.getQuestionRound())
                    .build());
        }

        previous.setGlobalCorrect(allTargetsCorrect);

        courseAttemptStore.updateAnswerMetaAndItems(previous);

        XpAward xpAward = null;
        if (!srsEvents.isEmpty()) {
            xpAward = knowledgeService.recordBatchResults(course.getUser(), srsEvents);
        }

        updateRecentStatsAfterAnswer(course, previous);

        // Emit next attempt
        Long lastPersonId = lastTargetPersonId(previous);

        CourseQuestionAttempt nextAttempt = findNextDue(course, lastPersonId, allTargetsCorrect, false);
        if (nextAttempt == null) {
            throw new NextQuestionUnavailableException();
        }

        int baseRound = previous.getQuestionRound() > 0 ? previous.getQuestionRound() : course.getCurrentRound();
        int nextRound = baseRound + 1;
        nextAttempt.setQuestionRound(nextRound);

        CourseQuestionPlan nextPlan = buildPlan(course, previous, nextAttempt, null, null, null);
        nextAttempt.setPlan(nextPlan);

        var runtimeNext = courseQuizQuestionBuilder.buildFromAttempt(nextAttempt, nextPlan);
        List<TruthAttributeValue> frozenNext = snapshotFactory.freezeTruthForQuestion(runtimeNext);
        nextAttempt.setSnapshot(snapshotFactory.fromQuestion(runtimeNext, frozenNext, targetPersonIds(nextAttempt)));

        CourseQuestionAttempt persistedNext = courseAttemptStore.create(nextAttempt);
        ensureSnapshotCourseQuestionId(persistedNext);

        course.setCurrentRound(nextRound);
        updateCourse(course);

        var nextQuestion = QuizQuestionSnapshotMapper.toQuestion(persistedNext.getSnapshot());

        // Return final result (QuizEngine will add handle on question if you want; here
        // we only provide nextQuestion)
        return new QuizAnswerResult.Builder()
                .withCorrect(allTargetsCorrect)
                .withFeedbackMessage(feedbackMessage)
                .withIsComplete(true)
                .withNextQuestion(nextQuestion)
                .withItemResults(itemResults)
                .withXpAward(xpAward)
                .build();
    }

    // ---------------------------------------------------------------------
    // Stats / Pools / Plan / helpers
    // ---------------------------------------------------------------------

    @Transactional(readOnly = true)
    public CourseStats getStats(Long courseId) {
        Long userId = userService.getCurrentIdOrThrow();
        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        if (course.getUser() == null || !Objects.equals(course.getUser().getId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }
        return buildStatsForCourse(course);
    }

    @Transactional
    private CourseQuestionAttempt findNextDue(
            Course course,
            Long lastPersonId,
            Boolean correct,
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

        return new CourseQuestionAttempt.Builder()
                .withCourse(course)
                .withQuestionRound(course.getCurrentRound() + 1)
                .withAskedAt(LocalDateTime.now())
                .withAnsweredAt(null)
                .withResponseTimeMs(0)
                .withRawSubmission(null)
                .withNormalizedAudit(null)
                .withGlobalCorrect(false)
                .withPoolType(selection.poolType())
                .withHelpUsed(false)
                .withItems(List.of(target))
                .build();
    }

    private void ensureSnapshotCourseQuestionId(CourseQuestionAttempt attempt) {
        if (attempt == null || attempt.getId() == null || attempt.getSnapshot() == null) {
            return;
        }
        if (attempt.getSnapshot().getContext() == null) {
            return;
        }
        if (attempt.getSnapshot().getContext().getCourseQuestionId() == null) {
            attempt.getSnapshot().getContext().setCourseQuestionId(attempt.getId());
        }
    }

    private void updateRecentStatsAfterAnswer(Course course, CourseQuestionAttempt attempt) {
        if (course == null || attempt == null) {
            return;
        }
        QuizFormat format = null;
        if (attempt.getSnapshot() != null) {
            format = attempt.getSnapshot().getFormat();
        } else if (attempt.getPlan() != null) {
            format = attempt.getPlan().getFormat();
        }
        courseRecentStatsService.upsertOnAnswer(
                course.getId(),
                format,
                attempt.isGlobalCorrect(),
                attempt.isHelpUsed(),
                attempt.getResponseTimeMs(),
                attempt.getAnsweredAt());
    }

    private SessionStats buildSessionStats(Course course) {
        if (course == null || course.getId() == null) {
            return SessionStats.empty();
        }

        CourseRecentStats recentStats = courseRecentStatsService.getStatsForCourse(course.getId());
        List<RecentAnswerStat> recentAnswers = courseAttemptStore.findRecentAnswerStats(course, SESSION_STATS_LIMIT);

        if (recentStats == null && recentAnswers.isEmpty()) {
            return SessionStats.empty();
        }

        int answered = recentAnswers.size();
        int correctCount = 0;
        for (RecentAnswerStat stat : recentAnswers) {
            if (stat != null && stat.correct()) {
                correctCount++;
            }
        }

        int correctStreak = 0;
        int errorStreak = 0;
        if (!recentAnswers.isEmpty() && recentAnswers.get(0) != null) {
            boolean lastCorrect = recentAnswers.get(0).correct();
            int streak = 0;
            for (RecentAnswerStat stat : recentAnswers) {
                if (stat == null || stat.correct() != lastCorrect) {
                    break;
                }
                streak++;
            }
            if (lastCorrect) {
                correctStreak = streak;
            } else {
                errorStreak = streak;
            }
        } else if (recentStats != null) {
            errorStreak = recentStats.getErrorStreak();
        }

        double accuracy = answered > 0 ? (double) correctCount / answered : 0.0;

        long durationMs = 0;
        if (!recentAnswers.isEmpty()) {
            RecentAnswerStat oldest = recentAnswers.get(recentAnswers.size() - 1);
            if (oldest != null && oldest.answeredAt() != null) {
                durationMs = Math.max(0, ChronoUnit.MILLIS.between(oldest.answeredAt(), LocalDateTime.now()));
            }
        }

        int formatStreak = recentStats != null ? recentStats.getFormatStreak() : 0;
        QuizFormat lastFormat = recentStats != null ? recentStats.getLastFormat() : null;

        return SessionStats.builder()
                .answered(answered)
                .correctStreak(correctStreak)
                .errorStreak(errorStreak)
                .accuracy(accuracy)
                .durationMs(durationMs)
                .formatStreak(formatStreak)
                .lastFormat(lastFormat)
                .build();
    }

    private CourseQuestionPlan buildPlan(
            Course course,
            CourseQuestionAttempt previousAttempt,
            CourseQuestionAttempt nextAttempt,
            QuizPreferredFormat preferredFormat,
            Boolean timed,
            Integer timeLimitMs) {
        Objects.requireNonNull(course, "course");
        Objects.requireNonNull(nextAttempt, "nextAttempt");

        Knowledge primary = primaryKnowledgeFromAttempt(nextAttempt);
        if (primary == null) {
            throw new IllegalStateException("CourseQuestionAttempt has no primary knowledge target");
        }

        Long userId = course.getUser() != null ? course.getUser().getId() : null;
        if (userId == null) {
            throw new IllegalStateException("Course.user is required");
        }
        Long gameModeId = course.getGameMode() != null ? course.getGameMode().getId() : null;
        if (gameModeId == null) {
            throw new IllegalStateException("Course.gameMode is required");
        }

        Long lastPersonId = lastTargetPersonId(previousAttempt);

        PlanningContext context = PlanningContext.forCourse(course.getId());
        SessionStats sessionStats = context.sessionTracking() ? buildSessionStats(course) : null;
        PlanningRequest request = PlanningRequest.builder()
                .userId(userId)
                .gameModeId(gameModeId)
                .gameOptions(courseOptionsResolver.resolve(course))
                .context(context)
                .course(course)
                .primaryKnowledge(primary)
                .selectedPoolType(nextAttempt.getPoolType())
                .sessionStats(sessionStats)
                .preferredFormat(preferredFormat)
                .requestedTimed(timed)
                .requestedTimeLimitMs(timeLimitMs)
                .lastPersonId(lastPersonId)
                .lastCorrect(previousAttempt != null ? previousAttempt.isGlobalCorrect() : null)
                .build();

        QuestionPlan planned = questionPlanningService.plan(request);

        CourseQuizPlanPolicy.Plan legacyPlan = null;
        List<Knowledge> extraTargets = List.of();
        if (coursePlanMode != CoursePlanMode.NEW) {
            extraTargets = context.multiTargetAllowed()
                    ? selectExtraTargets(course, primary, MULTI_TARGET_MIN - 1, lastPersonId)
                    : List.of();

            KnowledgeStats primaryStats = loadKnowledgeStats(userId, gameModeId, primary);
            List<KnowledgeStats> extraStats = loadStatsForKnowledges(userId, gameModeId, extraTargets);
            boolean primaryEligible = isMultiTargetEligible(primaryStats);
            boolean multiTargetAvailable = context.multiTargetAllowed()
                    && primaryEligible
                    && extraTargets.size() >= MULTI_TARGET_MIN - 1;

            legacyPlan = courseQuizPlanPolicy.decide(
                    course,
                    previousAttempt,
                    primary,
                    primaryStats,
                    extraStats,
                    courseRecentStatsService.getStatsForCourse(course.getId()),
                    preferredFormat,
                    timed,
                    timeLimitMs,
                    multiTargetAvailable);

            if (legacyPlan != null && coursePlanMode != CoursePlanMode.LEGACY) {
                logPlanDiffIfNeeded(course, planned, planned.targetCount(), legacyPlan, request.selectedPoolType());
            }

            if (legacyPlan != null) {
                List<Knowledge> targets = selectTargets(primary, extraTargets, legacyPlan.targetCount());
                applyAttemptItems(nextAttempt, targets, List.of(), legacyPlan.format());
                return buildCoursePlanFromLegacy(legacyPlan, targets);
            }
        }

        QuestionPlan effectivePlan = planned;
        List<Knowledge> targets = planned.targetKnowledges() != null && !planned.targetKnowledges().isEmpty()
                ? planned.targetKnowledges()
                : List.of(primary);

        if (effectivePlan.isMultiTarget() && targets.size() < effectivePlan.targetCount()) {
            effectivePlan = questionPlanningService.plan(
                    toSingleTargetRequest(request, context, lastPersonId, previousAttempt));
            targets = effectivePlan.targetKnowledges() != null && !effectivePlan.targetKnowledges().isEmpty()
                    ? effectivePlan.targetKnowledges()
                    : List.of(primary);
        }

        applyAttemptItems(nextAttempt, targets, effectivePlan.selectedDistractors(), effectivePlan.format());
        return buildCoursePlanFromPlanning(effectivePlan, targets);
    }

    private PlanningRequest toSingleTargetRequest(
            PlanningRequest request,
            PlanningContext context,
            Long lastPersonId,
            CourseQuestionAttempt previousAttempt) {
        PlanningContext fallbackContext = disableMultiTarget(context);
        return PlanningRequest.builder()
                .userId(request.userId())
                .gameModeId(request.gameModeId())
                .gameOptions(request.gameOptions())
                .context(fallbackContext)
                .course(request.course())
                .primaryKnowledge(request.primaryKnowledge())
                .selectedPoolType(request.selectedPoolType())
                .sessionStats(request.sessionStats())
                .preferredFormat(request.preferredFormat())
                .requestedTimed(request.requestedTimed())
                .requestedTimeLimitMs(request.requestedTimeLimitMs())
                .lastPersonId(lastPersonId)
                .lastCorrect(previousAttempt != null ? previousAttempt.isGlobalCorrect() : null)
                .build();
    }

    private Knowledge primaryKnowledgeFromAttempt(CourseQuestionAttempt attempt) {
        if (attempt == null || attempt.getItems() == null) {
            return null;
        }
        return attempt.getItems().stream()
                .filter(it -> it.getRole() == QuizQuestionItemRole.TARGET)
                .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                .map(CourseQuestionItem::getKnowledge)
                .findFirst()
                .orElse(null);
    }

    private KnowledgeStats loadKnowledgeStats(Long userId, Long gameModeId, Knowledge knowledge) {
        if (knowledge == null || knowledge.getId() == null) {
            return null;
        }
        Map<Long, KnowledgeStats> statsMap = knowledgeStatsService.getStatsForKnowledgeIds(
                userId,
                gameModeId,
                List.of(knowledge.getId()));
        return statsMap.get(knowledge.getId());
    }

    private List<KnowledgeStats> loadStatsForKnowledges(
            Long userId,
            Long gameModeId,
            List<Knowledge> knowledges) {
        if (knowledges == null || knowledges.isEmpty()) {
            return List.of();
        }
        List<Long> ids = knowledges.stream()
                .map(Knowledge::getId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, KnowledgeStats> statsMap = knowledgeStatsService.getStatsForKnowledgeIds(userId, gameModeId, ids);
        return ids.stream()
                .map(statsMap::get)
                .toList();
    }

    private List<Knowledge> selectExtraTargets(
            Course course,
            Knowledge primary,
            int count,
            Long lastPersonId) {
        if (count <= 0) {
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
                count,
                lastPersonId,
                constraints);
    }

    private boolean isMultiTargetEligible(KnowledgeStats stats) {
        if (stats == null) {
            return false;
        }
        return stats.getErrorStreak() <= MULTI_TARGET_MAX_ERROR_STREAK
                && stats.getAvgRtRecent() <= MULTI_TARGET_MAX_AVG_RT_MS
                && stats.getHelpRecent() <= MULTI_TARGET_MAX_HELP_RECENT
                && stats.getAttemptsRecent() >= MULTI_TARGET_MIN_ATTEMPTS_RECENT;
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

    private List<Knowledge> selectTargets(
            Knowledge primary,
            List<Knowledge> extraTargets,
            int targetCount) {
        List<Knowledge> targets = new ArrayList<>();
        if (primary != null) {
            targets.add(primary);
        }
        int needed = Math.max(0, targetCount - targets.size());
        if (needed > 0 && extraTargets != null) {
            for (Knowledge k : extraTargets) {
                if (k == null) {
                    continue;
                }
                targets.add(k);
                if (targets.size() >= targetCount) {
                    break;
                }
            }
        }
        return targets;
    }

    private void applyAttemptItems(
            CourseQuestionAttempt attempt,
            List<Knowledge> targets,
            List<Person> distractors,
            QuizFormat format) {
        List<CourseQuestionItem> items = new ArrayList<>();
        int position = 0;
        List<Long> targetPersonIds = targets.stream()
                .map(k -> k.getPerson() != null ? k.getPerson().getId() : null)
                .filter(Objects::nonNull)
                .toList();

        for (Knowledge knowledge : targets) {
            if (knowledge == null || knowledge.getPerson() == null) {
                continue;
            }
            items.add(new CourseQuestionItem.Builder()
                    .withPosition(position++)
                    .withRole(QuizQuestionItemRole.TARGET)
                    .withKnowledge(knowledge)
                    .withPerson(knowledge.getPerson())
                    .withAnswered(false)
                    .withCorrect(null)
                    .withNormalizedAnswer(null)
                    .build());
        }

        if (format == QuizFormat.MCQ && distractors != null && !distractors.isEmpty()) {
            for (Person p : distractors) {
                if (p == null || p.getId() == null) {
                    continue;
                }
                if (targetPersonIds.contains(p.getId())) {
                    continue;
                }
                items.add(new CourseQuestionItem.Builder()
                        .withPosition(position++)
                        .withRole(QuizQuestionItemRole.DISTRACTOR)
                        .withKnowledge(null)
                        .withPerson(p)
                        .withAnswered(false)
                        .withCorrect(null)
                        .withNormalizedAnswer(null)
                        .build());
            }
        }

        if (!items.isEmpty()) {
            attempt.setItems(items);
        }
    }

    private CourseQuestionPlan buildCoursePlanFromPlanning(QuestionPlan plan, List<Knowledge> targets) {
        List<Long> targetKnowledgeIds = targets.stream()
                .map(Knowledge::getId)
                .filter(Objects::nonNull)
                .toList();
        String paramsJson = plan.format() == QuizFormat.MCQ ? "{\"nbChoices\":4}" : null;
        return new CourseQuestionPlan.Builder()
                .withFormat(plan.format())
                .withTimed(Boolean.TRUE.equals(plan.timed()))
                .withTimeLimitMs(plan.timeLimitMs())
                .withTargetCount(targetKnowledgeIds.size())
                .withTargetKnowledgeIds(targetKnowledgeIds)
                .withParamsJson(paramsJson)
                .withReasonCode(plan.reasonCode())
                .withReasonDetailsJson(plan.reasonDetailsJson())
                .build();
    }

    private CourseQuestionPlan buildCoursePlanFromLegacy(
            CourseQuizPlanPolicy.Plan plan,
            List<Knowledge> targets) {
        List<Long> targetKnowledgeIds = targets.stream()
                .map(Knowledge::getId)
                .filter(Objects::nonNull)
                .toList();
        return new CourseQuestionPlan.Builder()
                .withFormat(plan.format())
                .withTimed(plan.timed())
                .withTimeLimitMs(plan.timeLimitMs())
                .withTargetCount(targetKnowledgeIds.size())
                .withTargetKnowledgeIds(targetKnowledgeIds)
                .withParamsJson(plan.paramsJson())
                .withReasonCode(plan.reasonCode())
                .withReasonDetailsJson(plan.reasonDetailsJson())
                .build();
    }

    private void logPlanDiffIfNeeded(
            Course course,
            QuestionPlan newPlan,
            int newTargetCount,
            CourseQuizPlanPolicy.Plan legacyPlan,
            PoolType poolType) {
        boolean different = legacyPlan.format() != newPlan.format()
                || legacyPlan.timed() != Boolean.TRUE.equals(newPlan.timed())
                || !Objects.equals(legacyPlan.timeLimitMs(), newPlan.timeLimitMs())
                || legacyPlan.targetCount() != newTargetCount
                || legacyPlan.reasonCode() != newPlan.reasonCode();
        if (!different) {
            return;
        }
        String msg = "Course plan divergence: courseId=" + course.getId()
                + " legacyFormat=" + legacyPlan.format()
                + " newFormat=" + newPlan.format()
                + " legacyTimed=" + legacyPlan.timed()
                + " newTimed=" + newPlan.timed()
                + " legacyTimeLimitMs=" + legacyPlan.timeLimitMs()
                + " newTimeLimitMs=" + newPlan.timeLimitMs()
                + " legacyTargetCount=" + legacyPlan.targetCount()
                + " newTargetCount=" + newTargetCount
                + " poolType=" + poolType
                + " legacyReason=" + legacyPlan.reasonCode()
                + " newReason=" + newPlan.reasonCode();
        if (coursePlanMode == CoursePlanMode.SHADOW) {
            log.info(msg);
        } else {
            log.debug(msg);
        }
    }

    private enum CoursePlanMode {
        NEW,
        LEGACY,
        SHADOW;

        private static CoursePlanMode from(String raw) {
            if (raw == null || raw.isBlank()) {
                return NEW;
            }
            try {
                return CoursePlanMode.valueOf(raw.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                return NEW;
            }
        }
    }

    private List<Long> targetPersonIds(CourseQuestionAttempt h) {
        if (h == null || h.getItems() == null)
            return List.of();
        return h.getItems().stream()
                .filter(it -> it.getRole() == QuizQuestionItemRole.TARGET)
                .map(it -> it.getPerson() != null ? it.getPerson().getId() : null)
                .filter(Objects::nonNull)
                .toList();
    }

    private Long lastTargetPersonId(CourseQuestionAttempt h) {
        if (h == null || h.getItems() == null)
            return null;
        return h.getItems().stream()
                .filter(it -> it.getRole() == QuizQuestionItemRole.TARGET)
                .sorted((a, b) -> Integer.compare(a.getPosition(), b.getPosition()))
                .findFirst()
                .map(it -> it.getKnowledge().getPerson().getId())
                .orElse(null);
    }
}
