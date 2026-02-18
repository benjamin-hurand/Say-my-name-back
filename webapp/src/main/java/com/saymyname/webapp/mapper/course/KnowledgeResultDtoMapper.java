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

        return KnowledgeResultEvent.builder()
                .knowledgeId(dto.knowledgeId())
                .gameModeId(dto.gameModeId())
                .personId(dto.personId())
                .correct(dto.isCorrect())
                .helpUsed(dto.helpUsed())
                .courseId(dto.courseId())
                .courseQuestionAttemptId(dto.CourseQuestionAttemptId())
                .questionRound(dto.questionRound())
                .build();
    }
}
