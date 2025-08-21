package com.saymyname.persistence.dao.course;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.course.CourseQuestionHistory;
import com.saymyname.persistence.entity.course.CourseQuestionHistoryEntity;
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

    public void update(CourseQuestionHistory courseQuestion) {
        courseQuestionHistoryRepository.save(mapper.toEntity(courseQuestion));
    }

}
