// src/main/java/com/saymyname/webapp/dto/course/CourseAnswerDto.java
package com.saymyname.webapp.dto.course;

import com.saymyname.webapp.dto.quiz.QuizAnswerSubmissionDto;

public record CourseAnswerDto(
                Long questionId,
                QuizAnswerSubmissionDto submission) {
}
