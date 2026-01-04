// src/main/java/com/saymyname/webapp/dto/auth/SessionDto.java
package com.saymyname.webapp.dto.auth;

import java.util.List;

import com.saymyname.webapp.dto.organization.UserOrganizationDto;

public record SessionDto(
        String publicUserId,
        String displayName,
        boolean isAdmin,
        List<UserOrganizationDto> organizations,
        List<String> emails) {
}
