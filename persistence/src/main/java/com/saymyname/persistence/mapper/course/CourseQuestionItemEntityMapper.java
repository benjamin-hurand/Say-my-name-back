package com.saymyname.persistence.mapper.course;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.CourseQuestionItem;
import com.saymyname.core.model.enums.course.QuizQuestionItemRole;
import com.saymyname.persistence.entity.organization.course.CourseQuestionItemEntity;
import com.saymyname.persistence.entity.organization.course.KnowledgeEntity;

@Component
public class CourseQuestionItemEntityMapper {

    public CourseQuestionItemEntityMapper() {
    }

    public CourseQuestionItemEntity toEntity(CourseQuestionItem model) {
        if (model == null)
            return null;

        CourseQuestionItemEntity entity = CourseQuestionItemEntity.builder().build();
        entity.setId(model.getId());
        entity.setAttemptId(model.getAttemptId());
        entity.setPosition(model.getPosition());
        entity.setRole(mapRole(model.getRole()));
        entity.setAnswered(model.isAnswered());
        entity.setCorrect(model.getCorrect());
        entity.setNormalizedAnswer(model.getNormalizedAnswer());

        if (model.getKnowledgeId() != null) {
            entity.setKnowledge(KnowledgeEntity.builder().id(model.getKnowledgeId()).build());
        } else {
            entity.setKnowledge(null);
        }

        entity.setPersonId(model.getPersonId());
        return entity;
    }

    public CourseQuestionItem toModel(CourseQuestionItemEntity entity) {
        if (entity == null)
            return null;

        return CourseQuestionItem.builder()
                .id(entity.getId())
                .attemptId(entity.getAttemptId())
                .position(entity.getPosition())
                .role(mapRole(entity.getRole()))
                .knowledgeId(entity.getKnowledge() != null ? entity.getKnowledge().getId() : null)
                .personId(entity.getPersonId())
                .answered(entity.isAnswered())
                .correct(entity.getCorrect())
                .normalizedAnswer(entity.getNormalizedAnswer())
                .build();
    }

    private CourseQuestionItemEntity.ItemRole mapRole(QuizQuestionItemRole role) {
        return role == null ? null : CourseQuestionItemEntity.ItemRole.valueOf(role.name());
    }

    private QuizQuestionItemRole mapRole(CourseQuestionItemEntity.ItemRole role) {
        return role == null ? null : QuizQuestionItemRole.valueOf(role.name());
    }
}
