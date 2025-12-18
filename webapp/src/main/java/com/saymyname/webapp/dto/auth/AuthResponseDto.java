// src/main/java/com/saymyname/webapp/dto/auth/AuthResponseDto.java
package com.saymyname.webapp.dto.auth;

import java.util.List;

import com.saymyname.webapp.dto.organization.UserOrganizationDto;

public record AuthResponseDto(
                String bearerToken,
                String publicUserId,
                String displayName,
                boolean isAdmin,
                List<UserOrganizationDto> organizations) {
}
