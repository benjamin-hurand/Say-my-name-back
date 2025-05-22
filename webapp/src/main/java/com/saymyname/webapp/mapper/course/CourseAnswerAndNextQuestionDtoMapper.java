package com.saymyname.webapp.mapper.course;

import java.util.List;
import java.util.Collections;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.AnswerAndNextQuestion;
import com.saymyname.webapp.dto.course.CourseAnswerAndNextQuestionDto;
import com.saymyname.webapp.dto.course.ResultAttributeDto;
import com.saymyname.webapp.dto.course.StatusCountsDto;

@Component
public class CourseAnswerAndNextQuestionDtoMapper {

    private final CourseQuestionHistoryDtoMapper courseQuestionDtoMapper;
    private final ResultAttributeDtoMapper resultAttributeDtoMapper;

    public CourseAnswerAndNextQuestionDtoMapper(
            CourseQuestionHistoryDtoMapper courseQuestionDtoMapper,
            ResultAttributeDtoMapper resultAttributeDtoMapper) {
        this.courseQuestionDtoMapper = courseQuestionDtoMapper;
        this.resultAttributeDtoMapper = resultAttributeDtoMapper;
    }

    public CourseAnswerAndNextQuestionDto toDto(
            AnswerAndNextQuestion answerAndNextQuestion,
            StatusCountsDto statusCounts) {

        // On initialise toujours à une liste vide pour éviter les NPE
        List<ResultAttributeDto> resultAttrsDto = Collections.emptyList();

        // Si le domaine nous a fourni des attributs, on les mappe
        List<com.saymyname.core.model.course.ResultAttribute> domainAttrs = answerAndNextQuestion.getResultAttributes();
        if (domainAttrs != null && !domainAttrs.isEmpty()) {
            resultAttrsDto = domainAttrs.stream()
                    .map(resultAttributeDtoMapper::toDto)
                    .collect(Collectors.toList());
        }

        return new CourseAnswerAndNextQuestionDto(
                /* correct */ answerAndNextQuestion.isCorrect(),
                /* userAnswer */ answerAndNextQuestion.getUserAnswer(),
                /* correctAnswer */ answerAndNextQuestion.getCorrectAnswer(),
                /* feedbackMessage */ answerAndNextQuestion.getFeedbackMessage(),
                /* nextQuestion */ courseQuestionDtoMapper.toReducedDto(answerAndNextQuestion.getNextQuestion()),
                /* resultAttributes */ resultAttrsDto,
                /* statusCounts */ statusCounts);
    }
}
