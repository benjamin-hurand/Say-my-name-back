package com.saymyname.webapp.dto;

import java.util.List;

public record PersonDto(
        Long id,
        String displayName,
        List<FactDto> attributes,
        List<PhotoDto> photos) {
}
