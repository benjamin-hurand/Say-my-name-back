package com.saymyname.webapp.dto;

import java.util.List;

public record PersonDto(
                Long id,
                UserDto user,
                List<PersonAttributeDto> attributes,
                List<PhotoDto> photos) {
}
