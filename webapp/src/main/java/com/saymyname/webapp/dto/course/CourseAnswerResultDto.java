// src/main/java/com/saymyname/webapp/dto/course/CourseAnswerResultDto.java
package com.saymyname.webapp.dto.course;

import java.util.List;

import com.saymyname.webapp.dto.quiz.QuizAnswerItemResultDto;
import com.saymyname.webapp.dto.quiz.QuizQuestionDto;

public record CourseAnswerResultDto(
                boolean correct,
                String feedbackMessage,
                QuizQuestionDto nextQuestion,
                List<QuizAnswerItemResultDto> itemResults,

                StatusCountsDto statusCounts) {
}
