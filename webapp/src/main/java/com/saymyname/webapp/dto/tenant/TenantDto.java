package com.saymyname.webapp.dto.tenant;

import java.time.LocalDateTime;

import com.saymyname.core.model.enums.tenant.TenantKind;

public record TenantDto(
                Long id,
                TenantKind kind,

                // ORG-only
                String key,
                String name,
                Boolean active,

                // commun
                LocalDateTime createdAt,
                LocalDateTime updatedAt) {
}