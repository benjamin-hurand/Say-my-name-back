package com.saymyname.webapp.dto.tenant;

import java.time.LocalDateTime;

import com.saymyname.core.model.enums.OrgRole;

public record TenantMembershipDto(
                Long tenantId,
                OrgRole role,
                LocalDateTime createdAt,
                TenantDto tenant) {
}