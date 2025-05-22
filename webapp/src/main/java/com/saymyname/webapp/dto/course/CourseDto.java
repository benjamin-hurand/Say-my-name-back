package com.saymyname.webapp.dto.course;

import java.util.List;

import com.saymyname.core.model.enums.CourseStatus;

public record CourseDto(
                Long id,
                Long userId,
                Long gameModeId,
                Long sortingMethodAttributeId,
                String sortingOrder,
                CourseStatus status,
                List<Long> populationIds) {

}
