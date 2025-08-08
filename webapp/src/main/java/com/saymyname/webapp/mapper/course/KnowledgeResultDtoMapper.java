package com.saymyname.webapp.mapper.course;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.KnowledgeResultEvent;
import com.saymyname.webapp.dto.course.KnowledgeResultDto;

@Component
public class KnowledgeResultDtoMapper {

    public KnowledgeResultEvent toModel(KnowledgeResultDto dto) {
        return new KnowledgeResultEvent(dto.gameModeId(), dto.personId(), dto.isCorrect(),
                dto.helpUsed());
    }
}
