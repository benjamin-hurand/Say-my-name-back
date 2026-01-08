package com.saymyname.webapp.mapper.course;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.ResultAttribute;
import com.saymyname.webapp.dto.course.ResultAttributeDto;

@Component
public class ResultAttributeDtoMapper {

    public ResultAttributeDtoMapper() {
    }

    public ResultAttributeDto toDto(ResultAttribute ra) {
        if (ra == null)
            return null;
        return new ResultAttributeDto(
                ra.getAttribute() != null ? ra.getAttribute().getId() : null,
                ra.getAttribute() != null ? ra.getAttribute().getName() : null,
                ra.getValue(),
                ra.isCorrect(),
                ra.isTarget());
    }
}
