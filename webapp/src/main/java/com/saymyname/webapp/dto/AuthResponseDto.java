package com.saymyname.webapp.dto;

import com.saymyname.core.model.enums.SrsAlgorithm;

public record AuthResponseDto(
        String bearerToken,
        Long userId,
        String username,
        String email,
        String roles,
        SrsAlgorithm srsAlgorithm) {
}
