// src/main/java/com/saymyname/persistence/dao/course/CourseQuestionAttemptDao.java
package com.saymyname.persistence.dao.course;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;

import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.CourseQuestionAttempt;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.persistence.entity.organization.course.CourseQuestionAttemptEntity;
import com.saymyname.persistence.mapper.course.CourseQuestionAttemptEntityMapper;
import com.saymyname.persistence.repository.course.CourseQuestionAttemptRepository;
import com.saymyname.persistence.repository.course.CourseQuestionItemRepository;
import com.saymyname.core.model.course.RecentAnswerStat;

@Repository
public class CourseQuestionAttemptDao {

    private final CourseQuestionAttemptRepository repo;
    private final CourseQuestionItemRepository itemRepo;
    private final CourseQuestionAttemptEntityMapper mapper;

    public CourseQuestionAttemptDao(
            CourseQuestionAttemptRepository repo,
            CourseQuestionItemRepository itemRepo,
            CourseQuestionAttemptEntityMapper mapper) {
        this.repo = repo;
        this.itemRepo = itemRepo;
        this.mapper = mapper;
    }

    /**
     * Sauvegarde un historique + items (cascade) et réinjecte l'id généré dans
     * l'objet métier.
     */
    @Transactional
    public CourseQuestionAttempt create(CourseQuestionAttempt attempt) {
        if (attempt == null) {
            throw new IllegalArgumentException("attempt cannot be null");
        }
        if (attempt.getSnapshot() == null) {
            throw new IllegalStateException("CourseQuestionAttempt.snapshot is required (Option B)");
        }
        if (attempt.getPlan() == null) {
            throw new IllegalStateException("CourseQuestionAttempt.plan is required");
        }

        CourseQuestionAttemptEntity entity = mapper.toEntity(attempt);
        entity = repo.save(entity);

        attempt.setId(entity.getId());
        return attempt;
    }

    /**
     * Find complet (avec items). Retourne null si absent.
     */
    @Transactional(readOnly = true)
    public CourseQuestionAttempt findById(Long id) {
        return repo.findByIdWithItems(id)
                .map(mapper::toModel)
                .orElse(null);
    }

    public void updateStepState(Long attemptId, QuizQuestionSnapshot snapshot, String rawSubmission,
            String normalizedAudit) {
        if (attemptId == null) {
            throw new IllegalArgumentException("attemptId cannot be null");
        }
        String stepStateJson = mapper.writeStepStateJson(snapshot, attemptId);

        int n = repo.updateStepStateAndAudit(attemptId, stepStateJson, rawSubmission, normalizedAudit);
        if (n == 0) {
            throw new IllegalArgumentException("Question not found: " + attemptId);
        }
    }

    @Transactional
    public void deleteAllByCourse(Course course) {
        if (course != null) {
            repo.deleteByCourseId(course.getId());
        }
    }

    /**
     * Regle: pas de save(mapper.toEntity(...)) pour update partiel d'un agregat
     * avec collection.
     */
    @Transactional
    public void updateAnswerMeta(CourseQuestionAttempt courseQuestion) {
        if (courseQuestion == null) {
            throw new IllegalArgumentException("courseQuestion cannot be null");
        }
        if (courseQuestion.getId() == null) {
            throw new IllegalStateException("courseQuestion.id is required for update");
        }
        int n = repo.updateAnswerMeta(
                courseQuestion.getId(),
                courseQuestion.getAnsweredAt(),
                courseQuestion.getResponseTimeMs(),
                courseQuestion.getRawSubmission(),
                courseQuestion.getNormalizedAudit(),
                courseQuestion.isGlobalCorrect());
        if (n == 0) {
            throw new IllegalArgumentException("Question not found: " + courseQuestion.getId());
        }
    }

    @Transactional
    public void updateTargetItemsAnswerMeta(Long attemptId, boolean answered, Boolean correct, String normalizedAnswer,
            boolean expectTargets) {
        if (attemptId == null) {
            throw new IllegalArgumentException("attemptId cannot be null");
        }
        int n = itemRepo.updateTargetItemsAnswerMeta(attemptId, answered, correct, normalizedAnswer);
        if (expectTargets && n == 0) {
            throw new IllegalStateException("No TARGET items updated for attemptId=" + attemptId);
        }
    }

    // ------- Stats activité -------
    @Transactional(readOnly = true)
    public int countAllAnswersByCourse(Course course) {
        if (course == null)
            return 0;
        return Math.toIntExact(repo.countByCourseIdTenant(course.getId()));
    }

    @Transactional(readOnly = true)
    public int countAnswersSince(Course course, LocalDateTime since) {
        if (course == null)
            return 0;
        if (since == null)
            throw new IllegalArgumentException("since cannot be null");
        return Math.toIntExact(repo.countByCourseIdAndAnsweredAtAfterTenant(course.getId(), since));
    }

    @Transactional(readOnly = true)
    public LocalDateTime findLastAnsweredAt(Course course) {
        if (course == null)
            return null;
        return repo.findLastAnsweredAt(course.getId());
    }

    @Transactional(readOnly = true)
    public List<RecentAnswerStat> findRecentAnswerStats(Course course, int limit) {
        if (course == null || course.getId() == null || limit <= 0) {
            return List.of();
        }
        return repo.findRecentAnsweredRows(course.getId(), PageRequest.of(0, limit)).stream()
                .map(row -> new RecentAnswerStat(row.isGlobalCorrect(), row.getAnsweredAt()))
                .toList();
    }

    @Transactional
    public void markHelpUsed(Long id) {
        int n = repo.markHelpUsed(id);
        if (n == 0)
            throw new IllegalArgumentException("Question not found: " + id);
    }
}
