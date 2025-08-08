package com.saymyname.webapp.dto.course;

import com.saymyname.webapp.dto.AttributeDto;
import com.saymyname.webapp.dto.ReducedUserDto;

public record PopulationDto(
                Long id,
                String title,
                String description,
                AttributeDto attributeFilter,
                String minValue,
                String maxValue,
                ReducedUserDto createdBy,
                Integer personCount) {

}
