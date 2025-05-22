package com.saymyname.webapp.dto.course;

import com.saymyname.webapp.dto.ReducedAttributeDto;

public record ResultAttributeDto(
        ReducedAttributeDto attribute,
        String value,
        Boolean isCorrect,
        Boolean isTarget) {

}
