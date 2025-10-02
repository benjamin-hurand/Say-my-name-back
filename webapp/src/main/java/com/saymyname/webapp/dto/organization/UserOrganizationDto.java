package com.saymyname.webapp.dto.organization;

import java.time.LocalDateTime;

public record UserOrganizationDto(
        Long organizationId,
        String organizationKey,
        String organizationName,
        String role,
        LocalDateTime createdAt) {
}
