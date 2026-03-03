package com.saymyname.webapp.dto.organization;

import java.time.LocalDateTime;

import com.saymyname.core.model.enums.OrgRole;

public record UserOrganizationDto(
        Long tenantId,
        String organizationKey,
        String organizationName,
        OrgRole role,
        LocalDateTime createdAt) {
}
