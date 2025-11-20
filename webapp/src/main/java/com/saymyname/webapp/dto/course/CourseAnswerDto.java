package com.saymyname.webapp.dto.course;

public record CourseAnswerDto(
        Long courseQuestionId,
        Long courseId,
        String answer

) {

}
