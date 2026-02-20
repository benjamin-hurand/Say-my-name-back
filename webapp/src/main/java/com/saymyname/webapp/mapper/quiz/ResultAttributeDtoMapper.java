// src/main/java/com/saymyname/webapp/mapper/quiz/ResultAttributeDtoMapper.java
package com.saymyname.webapp.mapper.quiz;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.ResultAttribute;
import com.saymyname.webapp.dto.quiz.ResultAttributeDto;

@Component
public class ResultAttributeDtoMapper {

    public ResultAttributeDto toDto(ResultAttribute ra) {
        if (ra == null)
            return null;

        return new ResultAttributeDto(
                ra.getAttribute() != null ? ra.getAttributeId() : null,
                ra.getAttribute() != null ? ra.getAttribute().getName() : null,
                ra.getValue(),
                ra.isCorrect(),
                ra.isTarget());
    }
}
