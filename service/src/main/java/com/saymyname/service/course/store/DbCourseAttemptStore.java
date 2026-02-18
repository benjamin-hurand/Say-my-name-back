// src/main/java/com/saymyname/service/course/store/DbCourseAttemptStore.java
package com.saymyname.service.course.store;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.CourseQuestionAttempt;
import com.saymyname.core.model.course.CourseQuestionItem;
import com.saymyname.core.model.course.CourseQuestionPlan;
import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.course.RecentAnswerStat;
import com.saymyname.core.model.enums.PoolType;
import com.saymyname.core.model.enums.course.QuizQuestionItemRole;
import com.saymyname.core.model.enums.quiz.QuizQuestionSource;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.core.model.quiz.QuizChoice;
import com.saymyname.core.model.quiz.QuizPayloadItem;
import com.saymyname.core.model.quiz.QuizQuestionContext;
import com.saymyname.core.model.quiz.QuizQuestionPayload;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.persistence.dao.course.CourseDao;
import com.saymyname.persistence.dao.course.KnowledgeDao; // ✅ ADAPTER AU NOM RÉEL
import com.saymyname.service.course.CourseQuestionAttemptService;
import com.saymyname.service.quiz.store.QuizAttemptStore;

@Component
public class DbCourseAttemptStore implements CourseAttemptStore, QuizAttemptStore {

    private final CourseQuestionAttemptService attemptService;
    private final CourseDao courseDao;
    private final KnowledgeDao knowledgeDao; // ✅

    public DbCourseAttemptStore(CourseQuestionAttemptService attemptService, CourseDao courseDao,
            KnowledgeDao knowledgeDao) {
        this.attemptService = attemptService;
        this.courseDao = courseDao;
        this.knowledgeDao = knowledgeDao;
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
        if (attempt.getAnsweredAt() != null) {
            return Optional.empty();
        }

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
    public AttemptHandle put(
            QuizQuestionSource source,
            Long userId,
            QuizQuestionSnapshot snapshot,
            long askedAtEpochMs,
            Long ttlSeconds) {

        if (source == null)
            throw new IllegalArgumentException("source is required");
        if (source != QuizQuestionSource.COURSE) {
            throw new UnsupportedOperationException("DbCourseAttemptStore only supports COURSE for QuizAttemptStore");
        }
        if (userId == null || userId <= 0)
            throw new IllegalArgumentException("userId is required");
        if (snapshot == null)
            throw new IllegalArgumentException("snapshot is required");

        QuizQuestionContext ctx = snapshot.getContext();
        if (ctx == null) {
            throw new IllegalStateException("snapshot.context is required for COURSE");
        }

        Long courseId = ctx.getCourseId();
        if (courseId == null || courseId <= 0) {
            throw new IllegalStateException("snapshot.context.courseId is required for COURSE");
        }

        Long knowledgeId = ctx.getKnowledgeId();
        if (knowledgeId == null || knowledgeId <= 0) {
            throw new IllegalStateException("snapshot.context.knowledgeId is required for COURSE (TARGET item)");
        }

        Course course = courseDao.findById(courseId)
                .orElseThrow(() -> new IllegalStateException("Course not found: id=" + courseId));

        Long ownerId = (course.getUser() != null) ? course.getUser().getId() : null;
        if (ownerId == null || !ownerId.equals(userId)) {
            throw new IllegalStateException("Forbidden: course does not belong to userId=" + userId);
        }

        Knowledge knowledge = knowledgeDao.findById(knowledgeId)
                .orElseThrow(() -> new IllegalStateException("Knowledge not found: id=" + knowledgeId));

        int round = (ctx.getQuestionRound() != null) ? ctx.getQuestionRound().intValue() : 0;
        PoolType poolType = (ctx.getPoolType() != null) ? ctx.getPoolType() : PoolType.NEW;

        LocalDateTime askedAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(askedAtEpochMs),
                ZoneId.systemDefault());

        // ✅ Item TARGET valide
        Long targetPersonId = (knowledge.getPerson() != null) ? knowledge.getPerson().getId() : null;

        List<CourseQuestionItem> items = new ArrayList<>();

        // Target item
        items.add(CourseQuestionItem.builder()
                .position(0)
                .role(QuizQuestionItemRole.TARGET)
                .knowledge(knowledge)
                .answered(false)
                .correct(null)
                .normalizedAnswer(null)
                .build());

        List<Long> payloadPersonIds = extractPayloadPersonIds(snapshot);
        int position = 1;
        for (Long personId : payloadPersonIds) {
            if (personId == null) {
                continue;
            }
            if (targetPersonId != null && targetPersonId.equals(personId)) {
                continue;
            }
            items.add(CourseQuestionItem.builder()
                    .position(position++)
                    .role(QuizQuestionItemRole.DISTRACTOR)
                    .person(Person.builder().id(personId).build())
                    .answered(false)
                    .correct(null)
                    .normalizedAnswer(null)
                    .build());
        }

        CourseQuestionPlan plan = CourseQuestionPlan.builder()
                .format(snapshot.getFormat())
                .timed(Boolean.TRUE.equals(snapshot.getTimed()))
                .timeLimitMs(snapshot.getTimeLimitMs())
                .targetCount(1)
                .targetKnowledgeIds(List.of(knowledgeId))
                .paramsJson(null)
                .reasonCode(snapshot.getReasonCode())
                .reasonDetailsJson(snapshot.getReasonDetailsJson())
                .build();

        CourseQuestionAttempt attempt = CourseQuestionAttempt.builder()
                .course(course)
                .questionRound(round)
                .askedAt(askedAt)
                .poolType(poolType)
                .snapshot(snapshot)
                .plan(plan)
                .items(items)
                .build();

        CourseQuestionAttempt saved = attemptService.create(attempt);

        return new AttemptHandle.DbIdHandle(saved.getId());
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
        if (source == null)
            throw new IllegalArgumentException("source is required");
        if (source != QuizQuestionSource.COURSE) {
            throw new UnsupportedOperationException("DbCourseAttemptStore only supports COURSE");
        }
        if (!(handle instanceof AttemptHandle.DbIdHandle dh)) {
            throw new IllegalArgumentException("Expected DbIdHandle for COURSE");
        }
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("userId is required");
        }

        Long attemptId = dh.value();

        CourseQuestionAttempt attempt = attemptService.findById(attemptId);
        if (attempt == null) {
            return Optional.empty();
        }

        Long ownerId = (attempt.getCourse() != null && attempt.getCourse().getUser() != null)
                ? attempt.getCourse().getUser().getId()
                : null;
        if (ownerId == null || !ownerId.equals(userId)) {
            return Optional.empty();
        }

        if (attempt.getAnsweredAt() != null) {
            return Optional.empty();
        }

        QuizQuestionSnapshot snapshot = attempt.getSnapshot();
        if (snapshot == null) {
            return Optional.empty();
        }

        boolean marked = attemptService.markAnsweredAtIfNull(attemptId, LocalDateTime.now());
        if (!marked) {
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

        return updateAttempt(attemptId, userId, updatedSnapshot, rawSubmission, normalizedAudit);
    }

    private static List<Long> extractPayloadPersonIds(QuizQuestionSnapshot snapshot) {
        if (snapshot == null) {
            return List.of();
        }
        QuizQuestionPayload payload = snapshot.getPayload();
        if (payload == null) {
            return List.of();
        }

        Set<Long> ids = new LinkedHashSet<>();

        List<QuizChoice> choices = payload.getChoices();
        if (choices != null) {
            for (QuizChoice c : choices) {
                if (c != null && c.getPersonId() != null) {
                    ids.add(c.getPersonId());
                }
            }
        }

        QuizChoice proposition = payload.getProposition();
        if (proposition != null && proposition.getPersonId() != null) {
            ids.add(proposition.getPersonId());
        }

        List<QuizPayloadItem> items = payload.getItems();
        if (items != null) {
            for (QuizPayloadItem item : items) {
                if (item != null && item.getPersonId() != null) {
                    ids.add(item.getPersonId());
                }
            }
        }

        if (ids.isEmpty()) {
            return List.of();
        }
        return List.copyOf(ids);
    }
}
