package com.saymyname.webapp.dto.organization;

import java.time.LocalDateTime;

import com.saymyname.core.model.enums.OrgRole;

public record UserOrganizationDto(
                Long organizationId,
                String organizationKey,
                String organizationName,
                OrgRole role,
                LocalDateTime createdAt) {
}
