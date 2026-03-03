package com.saymyname.webapp.dto.course;

import com.saymyname.core.model.enums.PopulationScope;

public record CreateCourseDto(
        Long targetAttributeId,
        PopulationScope populationScope) {

}
