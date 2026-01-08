// src/main/java/com/saymyname/persistence/dao/course/CourseQuestionHistoryDao.java
package com.saymyname.persistence.dao.course;

import java.time.LocalDateTime;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.CourseQuestionHistory;
import com.saymyname.persistence.entity.organization.course.CourseQuestionHistoryEntity;
import com.saymyname.persistence.mapper.course.CourseQuestionHistoryEntityMapper;
import com.saymyname.persistence.repository.course.CourseQuestionHistoryRepository;

@Repository
public class CourseQuestionHistoryDao {

    private final CourseQuestionHistoryRepository repo;
    private final CourseQuestionHistoryEntityMapper mapper;

    public CourseQuestionHistoryDao(
            CourseQuestionHistoryRepository repo,
            CourseQuestionHistoryEntityMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    /**
     * Sauvegarde un historique + items (cascade) et réinjecte l'id généré dans
     * l'objet métier.
     */
    @Transactional
    public CourseQuestionHistory create(CourseQuestionHistory history) {
        if (history == null) {
            throw new IllegalArgumentException("history cannot be null");
        }
        if (history.getSnapshot() == null) {
            throw new IllegalStateException("CourseQuestionHistory.snapshot is required (Option B)");
        }
        if (history.getPlan() == null) {
            throw new IllegalStateException("CourseQuestionHistory.plan is required");
        }

        CourseQuestionHistoryEntity entity = mapper.toEntity(history);
        entity = repo.save(entity);

        history.setId(entity.getId());
        return history;
    }

    /**
     * Find complet (avec items). Retourne null si absent.
     */
    @Transactional(readOnly = true)
    public CourseQuestionHistory findById(Long id) {
        return repo.findByIdWithItems(id)
                .map(mapper::toModel)
                .orElse(null);
    }

    @Transactional
    public void deleteAllByCourse(Course course) {
        if (course != null) {
            repo.deleteByCourseId(course.getId());
        }
    }

    /**
     * Update (history + items).
     */
    @Transactional
    public void update(CourseQuestionHistory courseQuestion) {
        if (courseQuestion == null) {
            throw new IllegalArgumentException("courseQuestion cannot be null");
        }
        if (courseQuestion.getId() == null) {
            throw new IllegalStateException("courseQuestion.id is required for update");
        }
        if (courseQuestion.getSnapshot() == null) {
            throw new IllegalStateException("CourseQuestionHistory.snapshot is required (Option B)");
        }
        if (courseQuestion.getPlan() == null) {
            throw new IllegalStateException("CourseQuestionHistory.plan is required");
        }

        repo.save(mapper.toEntity(courseQuestion));
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

    @Transactional
    public void markHelpUsed(Long id) {
        int n = repo.markHelpUsed(id);
        if (n == 0)
            throw new IllegalArgumentException("Question not found: " + id);
    }
}
