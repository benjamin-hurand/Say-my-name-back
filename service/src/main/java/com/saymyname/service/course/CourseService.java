// src/main/java/com/saymyname/service/course/CourseService.java
package com.saymyname.service.course;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.exception.course.CourseAlreadyExistsException;
import com.saymyname.core.exception.quiz.QuizUnprocessableException;
import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.CourseQuestionAttempt;
import com.saymyname.core.model.course.CourseStats;
import com.saymyname.core.model.enums.CourseStatus;
import com.saymyname.core.model.enums.PopulationScope;
import com.saymyname.core.model.quiz.QuizAnswerResult;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.planning.PreparedEmit;
import com.saymyname.persistence.dao.course.CourseDao;
import com.saymyname.service.UserService;
import com.saymyname.service.course.store.CourseAttemptStore;
import com.saymyname.service.quiz.QuizEngine;
import com.saymyname.service.quiz.QuizOrchestrationService;
import com.saymyname.service.quiz.handle.AttemptRef;
import com.saymyname.service.quiz.handle.QuizHandleCodec;
import com.saymyname.service.quiz.store.QuizAttemptStore.AttemptHandle;
import com.saymyname.core.model.enums.quiz.QuizQuestionSource;
import com.saymyname.core.model.course.KnowledgeResultEvent;
import com.saymyname.core.model.leaderboard.XpAward;

@Service
public class CourseService {

    private static final List<CourseStatus> ACTIVE_STATUSES = List.of(CourseStatus.IN_PROGRESS);

    private final CourseDao courseDao;
    private final KnowledgeService knowledgeService;
    private final CourseAttemptStore courseAttemptStore;
    private final UserService userService;

    private final QuizOrchestrationService quizOrchestrationService;
    private final QuizEngine quizEngine;
    private final QuizHandleCodec quizHandleCodec;

    public CourseService(
            CourseDao courseDao,
            KnowledgeService knowledgeService,
            CourseAttemptStore courseAttemptStore,
            UserService userService,
            QuizOrchestrationService quizOrchestrationService,
            QuizEngine quizEngine,
            QuizHandleCodec quizHandleCodec) {

        this.courseDao = Objects.requireNonNull(courseDao, "courseDao");
        this.knowledgeService = Objects.requireNonNull(knowledgeService, "knowledgeService");
        this.courseAttemptStore = Objects.requireNonNull(courseAttemptStore, "courseAttemptStore");
        this.userService = Objects.requireNonNull(userService, "userService");
        this.quizOrchestrationService = Objects.requireNonNull(quizOrchestrationService, "quizOrchestrationService");
        this.quizEngine = Objects.requireNonNull(quizEngine, "quizEngine");
        this.quizHandleCodec = Objects.requireNonNull(quizHandleCodec, "quizHandleCodec");
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
        if (course.getUser() == null || !course.getUser().getId().equals(userId)) {
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

    @Transactional(readOnly = true)
    public Course findById(Long courseId) {
        return courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));
    }

    @Transactional(readOnly = true)
    public List<Course> findAllByUser() {
        Long userId = userService.getCurrentIdOrThrow();
        return courseDao.findAllByUserAndStatusesOrderedByLastAccess(userId, ACTIVE_STATUSES);
    }

    @Transactional
    public void touchLastAccessed(Long courseId) {
        courseDao.touchLastAccessed(courseId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public CourseStats getStats(Long courseId) {
        Long userId = userService.getCurrentIdOrThrow();
        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        if (course.getUser() == null || !Objects.equals(course.getUser().getId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        int totalAnswers = courseAttemptStore.countAllAnswersByCourse(course);
        LocalDateTime lastActivity = courseAttemptStore.findLastAnsweredAt(course);
        return new CourseStats.Builder()
                .withCourseId(course.getId())
                .withTotalAnswers(totalAnswers)
                .withLastActivity(lastActivity)
                .build();
    }

    @Transactional(readOnly = true)
    public List<CourseStats> getStatsForUser() {
        Long userId = userService.getCurrentIdOrThrow();
        List<Course> courses = courseDao.findAllByUserAndStatusesOrderedByLastAccess(
                userId,
                List.of(CourseStatus.IN_PROGRESS, CourseStatus.ARCHIVED));

        return courses.stream().map(c -> {
            int totalAnswers = courseAttemptStore.countAllAnswersByCourse(c);
            LocalDateTime lastActivity = courseAttemptStore.findLastAnsweredAt(c);
            return new CourseStats.Builder()
                    .withCourseId(c.getId())
                    .withTotalAnswers(totalAnswers)
                    .withLastActivity(lastActivity)
                    .build();
        }).toList();
    }

    // ---------------------------------------------------------------------
    // Course quiz flow (stateless engine + orchestration)
    // ---------------------------------------------------------------------

    @Transactional
    public QuizQuestion continueCourse(Long courseId) {
        Long userId = userService.getCurrentIdOrThrow();

        Course course = findById(courseId);
        if (course.getUser() == null || !Objects.equals(course.getUser().getId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        PreparedEmit emit = quizOrchestrationService.orchestrateCourse(
                userId,
                courseId,
                null,
                null,
                null);

        return quizEngine.emitQuestion(userId, emit);
    }

    @Transactional
    public QuizAnswerResult answer(Long courseId, String questionHandle, QuizAnswerSubmission submission) {
        Long userId = userService.getCurrentIdOrThrow();

        Course course = findById(courseId);
        if (course.getUser() == null || !Objects.equals(course.getUser().getId(), userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden");
        }

        AttemptRef ref = quizHandleCodec.decodeOrThrow(questionHandle);
        if (ref.source() != QuizQuestionSource.COURSE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a COURSE question handle");
        }
        if (!(ref.handle() instanceof AttemptHandle.DbIdHandle dh)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid COURSE handle");
        }
        Long attemptId = dh.value();

        CourseQuestionAttempt existing = courseAttemptStore.findById(attemptId);
        if (existing == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course attempt not found");
        }
        if (existing.getCourse() == null || !Objects.equals(existing.getCourse().getId(), courseId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attempt does not belong to course");
        }
        if (existing.getAnsweredAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Question already answered");
        }

        PreparedEmit nextEmit = null;
        try {
            nextEmit = quizOrchestrationService.orchestrateCourse(
                    userId,
                    courseId,
                    null,
                    null,
                    null);
        } catch (QuizUnprocessableException e) {
            nextEmit = null;
        }

        QuizAnswerResult res = quizEngine.answerQuestion(userId, questionHandle, submission, nextEmit);

        if (isFinalAnswer(res)) {
            CourseQuestionAttempt attempt = courseAttemptStore.findById(attemptId);
            if (attempt == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Course attempt not found");
            }
            if (attempt.getCourse() == null || !Objects.equals(attempt.getCourse().getId(), courseId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Attempt does not belong to course");
            }

            LocalDateTime answeredAt = LocalDateTime.now();
            attempt.setAnsweredAt(answeredAt);
            attempt.setResponseTimeMs(computeResponseTimeMs(attempt.getAskedAt(), answeredAt));
            attempt.setGlobalCorrect(res.isCorrect());

            courseAttemptStore.updateAnswerMetaAndItems(attempt);

            List<KnowledgeResultEvent> events = buildKnowledgeEvents(attempt, res.isCorrect());
            if (!events.isEmpty()) {
                User me = userService.getCurrentAuthenticatedUserOrThrow();
                XpAward xpAward = knowledgeService.recordCourseAnswerResults(
                        me,
                        course,
                        attempt,
                        attempt.isHelpUsed(),
                        events);
                res.setXpAward(xpAward);
            }
        }

        return res;
    }

    private static boolean isFinalAnswer(QuizAnswerResult res) {
        if (res == null) {
            return false;
        }
        if (res.getIsComplete() == null) {
            return true;
        }
        return Boolean.TRUE.equals(res.getIsComplete());
    }

    private static int computeResponseTimeMs(LocalDateTime askedAt, LocalDateTime answeredAt) {
        if (askedAt == null || answeredAt == null) {
            return 0;
        }
        long ms = java.time.Duration.between(askedAt, answeredAt).toMillis();
        if (ms <= 0) {
            return 0;
        }
        return ms > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) ms;
    }

    private static List<KnowledgeResultEvent> buildKnowledgeEvents(CourseQuestionAttempt attempt, boolean correct) {
        if (attempt == null || attempt.getPlan() == null) {
            return List.of();
        }
        List<Long> knowledgeIds = attempt.getPlan().getTargetKnowledgeIds();
        if (knowledgeIds == null || knowledgeIds.isEmpty()) {
            return List.of();
        }

        Long courseId = attempt.getCourse() != null ? attempt.getCourse().getId() : null;
        Integer round = attempt.getQuestionRound();

        return knowledgeIds.stream()
                .filter(Objects::nonNull)
                .map(knowledgeId -> new KnowledgeResultEvent.Builder()
                        .withKnowledgeId(knowledgeId)
                        .withCorrect(correct)
                        .withHelpUsed(attempt.isHelpUsed())
                        .withCourseId(courseId)
                        .withCourseQuestionAttemptId(attempt.getId())
                        .withQuestionRound(round)
                        .build())
                .toList();
    }
}
