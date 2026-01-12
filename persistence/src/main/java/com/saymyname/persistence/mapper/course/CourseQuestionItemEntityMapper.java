package com.saymyname.persistence.mapper.course;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.CourseQuestionItem;
import com.saymyname.core.model.enums.course.QuizQuestionItemRole;
import com.saymyname.persistence.entity.organization.PersonEntity;
import com.saymyname.persistence.entity.organization.course.CourseQuestionItemEntity;
import com.saymyname.persistence.entity.organization.course.KnowledgeEntity;
import com.saymyname.persistence.mapper.PersonEntityMapper;

@Component
public class CourseQuestionItemEntityMapper {

    private final KnowledgeEntityMapper knowledgeMapper;
    private final PersonEntityMapper personMapper;

    public CourseQuestionItemEntityMapper(
            KnowledgeEntityMapper knowledgeMapper,
            PersonEntityMapper personMapper) {
        this.knowledgeMapper = knowledgeMapper;
        this.personMapper = personMapper;
    }

    public CourseQuestionItemEntity toEntity(CourseQuestionItem model) {
        if (model == null)
            return null;

        CourseQuestionItemEntity entity = new CourseQuestionItemEntity();
        entity.setId(model.getId());
        entity.setPosition(model.getPosition());
        entity.setRole(mapRole(model.getRole()));
        entity.setAnswered(model.isAnswered());
        entity.setCorrect(model.getCorrect());
        entity.setNormalizedAnswer(model.getNormalizedAnswer());

        // TARGET
        if (model.getKnowledge() != null) {
            KnowledgeEntity kn = knowledgeMapper.toEntity(model.getKnowledge());
            entity.setKnowledge(kn);
        } else {
            entity.setKnowledge(null);
        }

        // DISTRACTOR (et optionnel sur TARGET)
        if (model.getPerson() != null) {
            PersonEntity p = personMapper.toEntity(model.getPerson());
            entity.setPerson(p);
        } else {
            entity.setPerson(null);
        }

        return entity;
    }

    public CourseQuestionItem toModel(CourseQuestionItemEntity entity) {
        if (entity == null)
            return null;

        QuizQuestionItemRole role = mapRole(entity.getRole());

        return new CourseQuestionItem.Builder()
                .withId(entity.getId())
                .withPosition(entity.getPosition())
                .withRole(role)
                .withKnowledge(entity.getKnowledge() != null ? knowledgeMapper.toModel(entity.getKnowledge()) : null)
                .withPerson(entity.getPerson() != null ? personMapper.toShortModel(entity.getPerson()) : null)
                .withAnswered(entity.isAnswered())
                .withCorrect(entity.getCorrect())
                .withNormalizedAnswer(entity.getNormalizedAnswer())
                .build();
    }

    private CourseQuestionItemEntity.Role mapRole(QuizQuestionItemRole role) {
        return role == null ? null : CourseQuestionItemEntity.Role.valueOf(role.name());
    }

    private QuizQuestionItemRole mapRole(CourseQuestionItemEntity.Role role) {
        return role == null ? null : QuizQuestionItemRole.valueOf(role.name());
    }
}
