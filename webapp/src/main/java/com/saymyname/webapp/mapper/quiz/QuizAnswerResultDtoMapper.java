// src/main/java/com/saymyname/webapp/mapper/quiz/QuizAnswerResultDtoMapper.java
package com.saymyname.webapp.mapper.quiz;

import java.util.List;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.quiz.QuizAnswerItemResult;
import com.saymyname.core.model.quiz.QuizAnswerResult;
import com.saymyname.webapp.dto.quiz.QuizAnswerItemResultDto;
import com.saymyname.webapp.dto.quiz.QuizAnswerResultBaseDto;
import com.saymyname.webapp.dto.quiz.ResultAttributeDto;

@Component
public class QuizAnswerResultDtoMapper {

    private final ResultAttributeDtoMapper resultAttributeDtoMapper;
    private final QuizQuestionDtoMapper quizQuestionDtoMapper;

    public QuizAnswerResultDtoMapper(
            ResultAttributeDtoMapper resultAttributeDtoMapper,
            QuizQuestionDtoMapper quizQuestionDtoMapper) {
        this.resultAttributeDtoMapper = resultAttributeDtoMapper;
        this.quizQuestionDtoMapper = quizQuestionDtoMapper;
    }

    /**
     * Training answer result.
     * - Plus de "pont minimal": on mappe directement r.itemResults.
     * - nextQuestion vient de r.getNextQuestion() (souvent null en training batch).
     */
    public QuizAnswerResultBaseDto toDto(QuizAnswerResult r) {
        if (r == null) {
            return null;
        }

        List<QuizAnswerItemResultDto> itemDtos = r.getItemResults() == null
                ? List.of()
                : r.getItemResults().stream().map(this::toItemDto).toList();

        return new QuizAnswerResultBaseDto(
                r.isCorrect(),
                r.getFeedbackMessage(),
                r.getNextQuestion() == null ? null : quizQuestionDtoMapper.toDto(r.getNextQuestion()),
                itemDtos);
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
                r.getRole(),
                r.getKnowledgeId(),
                r.getPersonId(),
                r.isCorrect(),
                r.getUserAnswerNormalized(),
                r.getCorrectAnswer(),
                attrs);
    }
}
