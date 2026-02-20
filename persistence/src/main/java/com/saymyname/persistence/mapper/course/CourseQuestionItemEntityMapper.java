// src/main/java/com/saymyname/persistence/mapper/course/CourseQuestionItemEntityMapper.java
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
        if (model == null) {
            return null;
        }

        // Domain guard (aligné avec ton "riche + invariants")
        model.validateInvariants();

        CourseQuestionItemEntity entity = CourseQuestionItemEntity.builder().build();
        entity.setId(model.getId());
        entity.setPosition(model.getPosition());
        entity.setRole(model.getRole());
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

        // IMPORTANT:
        // attempt est géré par le parent (CourseQuestionAttemptEntityMapper) via
        // attempt.addItem(entity)
        // => on ne set pas attempt ici.

        return entity;
    }

    public CourseQuestionItem toModel(CourseQuestionItemEntity entity) {
        if (entity == null) {
            return null;
        }

        QuizQuestionItemRole role = entity.getRole();

        return CourseQuestionItem.builder()
                .id(entity.getId())
                .position(entity.getPosition())
                .role(role)
                .knowledge(entity.getKnowledge() != null ? knowledgeMapper.toModel(entity.getKnowledge()) : null)
                // comme avant : short model côté Person
                .person(entity.getPerson() != null ? personMapper.toShortModel(entity.getPerson()) : null)
                .answered(entity.isAnswered())
                .correct(entity.getCorrect())
                .normalizedAnswer(entity.getNormalizedAnswer())
                .build();
    }
}
