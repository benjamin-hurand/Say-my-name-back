package com.saymyname.webapp.mapper.course;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.ResultAttribute;
import com.saymyname.webapp.dto.course.ResultAttributeDto;
import com.saymyname.webapp.mapper.AttributeDtoMapper;

@Component
public class ResultAttributeDtoMapper {

    private final AttributeDtoMapper attributeDtoMapper;

    public ResultAttributeDtoMapper(AttributeDtoMapper attributeDtoMapper) {
        this.attributeDtoMapper = attributeDtoMapper;
    }

    public ResultAttributeDto toDto(ResultAttribute model) {
        return new ResultAttributeDto(
                attributeDtoMapper.toReducedDto(model.getAttribute()),
                model.getValue(),
                model.isCorrect(),
                model.isTarget());
    }
}
