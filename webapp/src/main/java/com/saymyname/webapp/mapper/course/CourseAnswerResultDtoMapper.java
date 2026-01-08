// src/main/java/com/saymyname/webapp/mapper/course/CourseAnswerResultDtoMapper.java
package com.saymyname.webapp.mapper.course;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.CourseAnswerItemResult;
import com.saymyname.core.model.course.CourseAnswerResult;
import com.saymyname.webapp.dto.course.CourseAnswerItemResultDto;
import com.saymyname.webapp.dto.course.CourseAnswerResultDto;
import com.saymyname.webapp.dto.course.ResultAttributeDto;
import com.saymyname.webapp.dto.course.StatusCountsDto;
import com.saymyname.webapp.mapper.quiz.QuizQuestionDtoMapper;

@Component
public class CourseAnswerResultDtoMapper {

    private final QuizQuestionDtoMapper quizQuestionDtoMapper;
    private final ResultAttributeDtoMapper resultAttributeDtoMapper;

    public CourseAnswerResultDtoMapper(
            QuizQuestionDtoMapper quizQuestionDtoMapper,
            ResultAttributeDtoMapper resultAttributeDtoMapper) {
        this.quizQuestionDtoMapper = quizQuestionDtoMapper;
        this.resultAttributeDtoMapper = resultAttributeDtoMapper;
    }

    public CourseAnswerResultDto toDto(CourseAnswerResult res, StatusCountsDto statusCounts) {
        if (res == null)
            return null;

        List<CourseAnswerItemResultDto> itemDtos = res.getItemResults() == null
                ? List.of()
                : res.getItemResults().stream().map(this::toItemDto).toList();

        return new CourseAnswerResultDto(
                res.isCorrect(),
                res.getRawSubmission(),
                res.getNormalizedSubmission(),
                res.getFeedbackMessage(),
                quizQuestionDtoMapper.toDto(res.getNextQuestion()),
                itemDtos,
                statusCounts);
    }

    private CourseAnswerItemResultDto toItemDto(CourseAnswerItemResult r) {
        if (r == null)
            return null;

        List<ResultAttributeDto> attrs = Collections.emptyList();
        var domain = r.getResultAttributes();
        if (domain != null && !domain.isEmpty()) {
            attrs = domain.stream().map(resultAttributeDtoMapper::toDto).toList();
        }

        return new CourseAnswerItemResultDto(
                r.getPosition(),
                r.getRole(),
                r.getKnowledgeId(),
                r.getPersonId(),
                r.isCorrect(),
                r.getUserAnswerNormalized(),
                r.getCorrectAnswer(),
                attrs);
    }
}
