// src/main/java/com/saymyname/webapp/mapper/course/KnowledgeResultDtoMapper.java
package com.saymyname.webapp.mapper.course;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.KnowledgeResultEvent;
import com.saymyname.webapp.dto.course.KnowledgeResultDto;

@Component
public class KnowledgeResultDtoMapper {

    public KnowledgeResultEvent toModel(KnowledgeResultDto dto) {
        if (dto == null)
            return null;

        return new KnowledgeResultEvent.Builder()
                .withKnowledgeId(dto.knowledgeId())
                .withFactId(dto.factId())
                .withCorrect(dto.isCorrect())
                .withHelpUsed(dto.helpUsed())
                .withCourseId(dto.courseId())
                .withCourseQuestionAttemptId(dto.courseQuestionAttemptId())
                .withQuestionRound(dto.questionRound())
                .build();
    }
}
