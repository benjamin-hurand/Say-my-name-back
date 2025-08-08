package com.saymyname.webapp.dto;

import com.saymyname.core.model.enums.SrsAlgorithm;

public record UserDto(
        Long id,
        String username,
        String email,
        SrsAlgorithm srsAlgorithm,
        String roles) {

}
