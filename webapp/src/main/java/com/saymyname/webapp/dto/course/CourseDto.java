package com.saymyname.webapp.dto.course;

import com.saymyname.core.model.enums.CourseStatus;
import com.saymyname.core.model.enums.PopulationScope;

public record CourseDto(
        Long id,
        Long userId,
        Long gameModeId,
        CourseStatus status,
        PopulationScope populationScope) {

}
