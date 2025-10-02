package com.saymyname.webapp.dto.auth;

import com.saymyname.core.model.enums.SrsAlgorithm;
import com.saymyname.webapp.dto.organization.UserOrganizationDto;

import java.util.List;

public record AuthResponseDto(
                String bearerToken,
                Long userId,
                String username,
                String email,
                String roles,
                SrsAlgorithm srsAlgorithm,
                List<UserOrganizationDto> organizations) {
}
