// src/main/java/com/saymyname/webapp/dto/auth/SessionDto.java
package com.saymyname.webapp.dto.auth;

import java.util.List;

import com.saymyname.webapp.dto.tenant.TenantMembershipDto;

public record SessionDto(
                String publicUserId,
                String displayName,
                boolean isAdmin,
                List<TenantMembershipDto> memberships,
                List<String> emails) {
}
