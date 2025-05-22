package com.saymyname.persistence.mapper.course;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.CourseQuestionHistory;
import com.saymyname.persistence.entity.course.CourseQuestionHistoryEntity;

@Component
public class CourseQuestionHistoryEntityMapper {

    private final CourseEntityMapper courseMapper;
    private final KnowledgeEntityMapper knowledgeMapper;

    public CourseQuestionHistoryEntityMapper(
            CourseEntityMapper courseMapper,
            KnowledgeEntityMapper knowledgeMapper) {
        this.courseMapper = courseMapper;
        this.knowledgeMapper = knowledgeMapper;
    }

    public CourseQuestionHistoryEntity toEntity(CourseQuestionHistory model) {
        if (model == null)
            return null;

        CourseQuestionHistoryEntity entity = new CourseQuestionHistoryEntity.Builder()
                .withId(model.getId())
                .withCourse(courseMapper.toEntity(model.getCourse()))
                .withKnowledge(knowledgeMapper.toEntity(model.getKnowledge()))
                .withQuestionRound(model.getQuestionRound())
                .withAskedAt(model.getAskedAt())
                .withAnsweredAt(model.getAnsweredAt())
                .withResponseTimeMs(model.getResponseTimeMs())
                .withUserAnswer(model.getUserAnswer())
                .withCorrect(model.isCorrect())
                .withPoolType(model.getPoolType())
                .withHelpUsed(model.isHelpUsed())
                .build();
        return entity;
    }

    public CourseQuestionHistory toModel(CourseQuestionHistoryEntity entity) {
        if (entity == null)
            return null;

        return new CourseQuestionHistory.Builder()
                .withId(entity.getId())
                .withCourse(courseMapper.toModel(entity.getCourse()))
                .withKnowledge(knowledgeMapper.toModel(entity.getKnowledge()))
                .withQuestionRound(entity.getQuestionRound())
                .withAskedAt(entity.getAskedAt())
                .withAnsweredAt(entity.getAnsweredAt())
                .withResponseTimeMs(entity.getResponseTimeMs())
                .withUserAnswer(entity.getUserAnswer())
                .withCorrect(entity.isCorrect())
                .withPoolType(entity.getPoolType())
                .withHelpUsed(entity.isHelpUsed())
                .build();
    }
}