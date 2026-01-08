// src/main/java/com/saymyname/webapp/dto/course/CourseAnswerResultDto.java
package com.saymyname.webapp.dto.course;

import java.util.List;

import com.saymyname.webapp.dto.quiz.QuizQuestionDto;

public record CourseAnswerResultDto(
                Boolean correct,
                String rawSubmission,
                String normalizedSubmission,
                String feedbackMessage,
                QuizQuestionDto nextQuestion,
                List<CourseAnswerItemResultDto> itemResults,
                StatusCountsDto statusCounts) {
}
