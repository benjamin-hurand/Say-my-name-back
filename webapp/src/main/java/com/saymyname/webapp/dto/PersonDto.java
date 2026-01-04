package com.saymyname.webapp.dto;

import java.util.List;

public record PersonDto(
                Long id,
                List<PersonAttributeDto> attributes,
                List<PhotoDto> photos) {
}
