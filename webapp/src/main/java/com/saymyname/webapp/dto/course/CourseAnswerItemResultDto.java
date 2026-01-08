// src/main/java/com/saymyname/webapp/dto/course/CourseAnswerItemResultDto.java
package com.saymyname.webapp.dto.course;

import java.util.List;

import com.saymyname.core.model.enums.course.CourseQuestionItemRole;

public record CourseAnswerItemResultDto(
        Integer position,
        CourseQuestionItemRole role,
        Long knowledgeId,
        Long personId,
        Boolean correct,
        String userAnswerNormalized,
        String correctAnswer,
        List<ResultAttributeDto> resultAttributes) {
}
