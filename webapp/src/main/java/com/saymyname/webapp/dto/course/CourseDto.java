// src/main/java/com/saymyname/webapp/dto/course/CourseDto.java
package com.saymyname.webapp.dto.course;

import com.saymyname.core.model.enums.course.CourseStatus;

public record CourseDto(
                Long id,
                Long userId,
                Long targetAttributeId,
                CourseStatus status,
                Integer currentRound) {
}
