// src/main/java/com/saymyname/webapp/mapper/quiz/TargetAnswerResultDtoMapper.java
package com.saymyname.webapp.mapper.quiz;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.TargetAnswerResult;
import com.saymyname.webapp.dto.quiz.TargetAnswerResultDto;

@Component
public class TargetAnswerResultDtoMapper {

    public TargetAnswerResultDto toDto(TargetAnswerResult ra) {
        if (ra == null)
            return null;

        return new TargetAnswerResultDto(
                ra.getAttributeId(),
                ra.getValue(),
                ra.isCorrect());
    }
}
