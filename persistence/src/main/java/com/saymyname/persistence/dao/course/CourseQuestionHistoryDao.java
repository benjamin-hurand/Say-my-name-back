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

    private final CourseQuestionHistoryRepository courseQuestionHistoryRepository;
    private final CourseQuestionHistoryEntityMapper mapper;

    public CourseQuestionHistoryDao(CourseQuestionHistoryRepository courseQuestionHistoryRepository,
            CourseQuestionHistoryEntityMapper mapper) {
        this.courseQuestionHistoryRepository = courseQuestionHistoryRepository;
        this.mapper = mapper;
    }

    /**
     * Sauvegarde un historique de question et enrichit l'objet métier avec l'id
     * généré.
     */
    @Transactional
    public CourseQuestionHistory create(CourseQuestionHistory history) {
        CourseQuestionHistoryEntity entity = mapper.toEntity(history);
        entity = courseQuestionHistoryRepository.save(entity);
        CourseQuestionHistory persisted = mapper.toModel(entity);
        // on remet l'id généré dans l'objet métier
        history.setId(persisted.getId());
        return history;
    }

    @Transactional
    public CourseQuestionHistory findById(Long id) {
        return courseQuestionHistoryRepository.findById(id)
                .map(mapper::toModel)
                .orElse(null);
    }

    public void deleteAllByCourse(Course course) {
        if (course != null) {
            courseQuestionHistoryRepository.deleteByCourseId(course.getId());
        }
    }

    public void update(CourseQuestionHistory courseQuestion) {
        courseQuestionHistoryRepository.save(mapper.toEntity(courseQuestion));
    }

    // ------- Stats activité -------
    public int countAllAnswersByCourse(Course course) {
        return Math.toIntExact(courseQuestionHistoryRepository.countByCourseId(course.getId()));
    }

    public int countAnswersSince(Course course, LocalDateTime since) {
        return Math
                .toIntExact(courseQuestionHistoryRepository.countByCourseIdAndAnsweredAtAfter(course.getId(), since));
    }

    public LocalDateTime findLastAnsweredAt(Course course) {
        return courseQuestionHistoryRepository.findLastAnsweredAt(course.getId());
    }
}
