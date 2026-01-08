// src/main/java/com/saymyname/webapp/mapper/quiz/QuizAnswerResultDtoMapper.java
package com.saymyname.webapp.mapper.quiz;

import java.util.List;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.ResultAttribute;
import com.saymyname.core.model.quiz.QuizAnswerResult;
import com.saymyname.webapp.dto.course.ResultAttributeDto;
import com.saymyname.webapp.dto.quiz.QuizAnswerResultDto;

@Component
public class QuizAnswerResultDtoMapper {

    public QuizAnswerResultDto toDto(QuizAnswerResult r) {
        if (r == null)
            return null;

        List<ResultAttributeDto> attrs = r.getResultAttributes() == null
                ? List.of()
                : r.getResultAttributes().stream().map(this::toDto).toList();

        return new QuizAnswerResultDto(
                r.isCorrect(),
                r.getUserAnswer(),
                r.getCorrectAnswer(),
                r.getFeedbackMessage(),
                attrs);
    }

    private ResultAttributeDto toDto(ResultAttribute ra) {
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
