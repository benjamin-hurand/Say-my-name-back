package com.saymyname.webapp.dto.course;

public record CourseAnswerDto(
                Long userId,
                Long courseQuestionId,
                Long courseId,
                String answer

) {

}
