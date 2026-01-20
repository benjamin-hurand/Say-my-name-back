// src/main/java/com/saymyname/service/course/store/DbCourseAttemptStore.java
package com.saymyname.service.course.store;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.CourseQuestionAttempt;
import com.saymyname.core.model.enums.quiz.QuizQuestionSource;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.service.course.CourseQuestionAttemptService;
import com.saymyname.core.model.course.RecentAnswerStat;
import com.saymyname.service.quiz.store.QuizAttemptStore;

/**
 * Adaptateur: CourseAttemptStore -> CourseQuestionAttemptService (DB).
 *
 * En plus, sert de backend QuizAttemptStore pour COURSE (handle = attemptId
 * DB).
 */
@Component
public class DbCourseAttemptStore implements CourseAttemptStore, QuizAttemptStore {

    private final CourseQuestionAttemptService attemptService;

    public DbCourseAttemptStore(CourseQuestionAttemptService attemptService) {
        this.attemptService = attemptService;
    }

    // -----------------------
    // CourseAttemptStore (course-aware)
    // -----------------------

    @Override
    public CourseQuestionAttempt create(CourseQuestionAttempt attempt) {
        return attemptService.create(attempt);
    }

    @Override
    public CourseQuestionAttempt findById(Long id) {
        return attemptService.findById(id);
    }

    @Override
    public void deleteAllByCourse(Course course) {
        attemptService.deleteAllByCourse(course);
    }

    @Override
    public void updateStepState(Long attemptId, QuizQuestionSnapshot snapShot, String rawSubmission,
            String normalizedAudit) {
        attemptService.updateStepState(attemptId, snapShot, rawSubmission, normalizedAudit);
    }

    @Override
    public void updateAnswerMetaAndItems(CourseQuestionAttempt courseQuestion) {
        attemptService.updateAnswerMetaAndItems(courseQuestion);
    }

    @Override
    public List<PersonAttribute> markHelpAndGetAttributes(Long courseId, Long questionId) {
        return attemptService.markHelpAndGetAttributes(courseId, questionId);
    }

    @Override
    public void markHelpUsed(Long id) {
        attemptService.markHelpUsed(id);
    }

    @Override
    public int countAllAnswersByCourse(Course course) {
        return attemptService.countAllAnswersByCourse(course);
    }

    @Override
    public int countAnswersSince(Course course, LocalDateTime since) {
        return attemptService.countAnswersSince(course, since);
    }

    @Override
    public LocalDateTime findLastAnsweredAt(Course course) {
        return attemptService.findLastAnsweredAt(course);
    }

    @Override
    public List<RecentAnswerStat> findRecentAnswerStats(Course course, int limit) {
        return attemptService.findRecentAnswerStats(course, limit);
    }

    // -----------------------
    // Bridge minimal CourseAttemptStore -> QuizAttemptStore
    // -----------------------

    @Override
    public Optional<StoredAttempt> peekAttempt(Long attemptId, Long userId) {
        if (attemptId == null || attemptId <= 0)
            return Optional.empty();
        if (userId == null || userId <= 0)
            return Optional.empty();

        CourseQuestionAttempt attempt = attemptService.findById(attemptId);
        if (attempt == null)
            return Optional.empty();

        // user-scoping
        Long ownerId = (attempt.getCourse() != null && attempt.getCourse().getUser() != null)
                ? attempt.getCourse().getUser().getId()
                : null;
        if (ownerId == null || !ownerId.equals(userId)) {
            return Optional.empty();
        }

        QuizQuestionSnapshot snapshot = attempt.getSnapshot();
        if (snapshot == null) {
            return Optional.empty();
        }

        long askedAtEpochMs = QuizAttemptStore.toEpochMs(attempt.getAskedAt());
        return Optional.of(new StoredAttempt(
                userId,
                snapshot,
                askedAtEpochMs,
                null,
                attempt.getRawSubmission(),
                attempt.getNormalizedAudit()));
    }

    @Override
    public boolean updateSnapshot(Long attemptId, Long userId, QuizQuestionSnapshot updatedSnapshot) {
        return updateAttempt(attemptId, userId, updatedSnapshot, null, null);
    }

    // -----------------------
    // QuizAttemptStore (unified facade) — COURSE backend
    // -----------------------

    @Override
    public AttemptHandle put(QuizQuestionSource source, Long userId, QuizQuestionSnapshot snapshot, long askedAtEpochMs,
            Long ttlSeconds) {
        if (source == null)
            throw new IllegalArgumentException("source is required");
        if (source != QuizQuestionSource.COURSE) {
            throw new UnsupportedOperationException("DbCourseAttemptStore only supports COURSE for QuizAttemptStore");
        }
        throw new UnsupportedOperationException(
                "COURSE put() is unsupported here because creating a course attempt requires plan/items/courseId/round. "
                        +
                        "Use CourseService emission (continueCourseAttempt) which persists a rich CourseQuestionAttempt.");
    }

    @Override
    public boolean updateAttempt(Long attemptId, Long userId, QuizQuestionSnapshot updatedSnapshot,
            String rawSubmission, String normalizedAudit) {
        if (updatedSnapshot == null)
            return false;

        CourseQuestionAttempt attempt = attemptService.findById(attemptId);
        if (attempt == null)
            return false;

        Long ownerId = (attempt.getCourse() != null && attempt.getCourse().getUser() != null)
                ? attempt.getCourse().getUser().getId()
                : null;
        if (ownerId == null || !ownerId.equals(userId)) {
            return false;
        }

        String raw = rawSubmission != null ? rawSubmission : attempt.getRawSubmission();
        String normalized = normalizedAudit != null ? normalizedAudit : attempt.getNormalizedAudit();

        attemptService.updateStepState(attemptId, updatedSnapshot, raw, normalized);
        return true;
    }

    @Override
    public Optional<StoredAttempt> peek(QuizQuestionSource source, AttemptHandle handle, Long userId) {
        if (source == null)
            throw new IllegalArgumentException("source is required");
        if (source != QuizQuestionSource.COURSE) {
            throw new UnsupportedOperationException("DbCourseAttemptStore only supports COURSE");
        }
        if (!(handle instanceof AttemptHandle.DbIdHandle dh)) {
            throw new IllegalArgumentException("Expected DbIdHandle for COURSE");
        }
        return peekAttempt(dh.value(), userId);
    }

    @Override
    public Optional<StoredAttempt> consume(QuizQuestionSource source, AttemptHandle handle, Long userId) {
        // COURSE: consume == peek
        return peek(source, handle, userId);
    }

    @Override
    public boolean updateSnapshot(QuizQuestionSource source, AttemptHandle handle, Long userId,
            QuizQuestionSnapshot updatedSnapshot) {
        if (source == null)
            throw new IllegalArgumentException("source is required");
        if (source != QuizQuestionSource.COURSE) {
            throw new UnsupportedOperationException("DbCourseAttemptStore only supports COURSE");
        }
        if (!(handle instanceof AttemptHandle.DbIdHandle dh)) {
            throw new IllegalArgumentException("Expected DbIdHandle for COURSE");
        }
        return updateSnapshot(dh.value(), userId, updatedSnapshot);
    }

    @Override
    public boolean updateAttempt(QuizQuestionSource source, AttemptHandle handle, Long userId,
            QuizQuestionSnapshot updatedSnapshot, String rawSubmission, String normalizedAudit) {

        if (source == null)
            throw new IllegalArgumentException("source is required");
        if (source != QuizQuestionSource.COURSE) {
            throw new UnsupportedOperationException("DbCourseAttemptStore only supports COURSE");
        }
        if (!(handle instanceof AttemptHandle.DbIdHandle dh)) {
            throw new IllegalArgumentException("Expected DbIdHandle for COURSE");
        }
        Long attemptId = dh.value();

        if (updatedSnapshot == null)
            return false;

        CourseQuestionAttempt attempt = attemptService.findById(attemptId);
        if (attempt == null)
            return false;

        Long ownerId = (attempt.getCourse() != null && attempt.getCourse().getUser() != null)
                ? attempt.getCourse().getUser().getId()
                : null;
        if (ownerId == null || !ownerId.equals(userId)) {
            return false;
        }

        String raw = rawSubmission != null ? rawSubmission : attempt.getRawSubmission();
        String norm = normalizedAudit != null ? normalizedAudit : attempt.getNormalizedAudit();

        attemptService.updateStepState(attemptId, updatedSnapshot, raw, norm);
        return true;
    }

}
