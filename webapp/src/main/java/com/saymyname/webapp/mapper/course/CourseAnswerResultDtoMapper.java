// src/main/java/com/saymyname/webapp/mapper/course/CourseAnswerResultDtoMapper.java
package com.saymyname.webapp.mapper.course;

import java.util.List;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.CourseAnswerResult;
import com.saymyname.core.model.quiz.QuizAnswerItemResult;
import com.saymyname.webapp.dto.course.CourseAnswerResultDto;
import com.saymyname.webapp.dto.course.StatusCountsDto;
import com.saymyname.webapp.dto.quiz.QuizAnswerItemResultDto;
import com.saymyname.webapp.dto.quiz.ResultAttributeDto;
import com.saymyname.webapp.mapper.quiz.QuizQuestionDtoMapper;
import com.saymyname.webapp.mapper.quiz.ResultAttributeDtoMapper;

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
        if (res == null) {
            return null;
        }

        List<QuizAnswerItemResultDto> itemDtos = res.getItemResults() == null
                ? List.of()
                : res.getItemResults().stream().map(this::toItemDto).toList();

        return new CourseAnswerResultDto(
                res.isCorrect(),
                res.getFeedbackMessage(),
                res.getNextQuestion() == null ? null : quizQuestionDtoMapper.toDto(res.getNextQuestion()),
                itemDtos,
                statusCounts);
    }

    private QuizAnswerItemResultDto toItemDto(QuizAnswerItemResult r) {
        if (r == null) {
            return null;
        }

        List<ResultAttributeDto> attrs = r.getResultAttributes() == null
                ? List.of()
                : r.getResultAttributes().stream().map(resultAttributeDtoMapper::toDto).toList();

        return new QuizAnswerItemResultDto(
                r.getPosition(),
                r.getRole(), // ✅ plus de conversion valueOf(name())
                r.getKnowledgeId(),
                r.getPersonId(),
                r.isCorrect(),
                r.getUserAnswerNormalized(),
                r.getCorrectAnswer(),
                attrs);
    }
}
