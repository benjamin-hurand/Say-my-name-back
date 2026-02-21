package com.saymyname.webapp.dto;

import java.util.List;

public record PersonDto(
        Long id,
        List<FactDto> attributes,
        List<PhotoDto> photos) {
}
