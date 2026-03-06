package com.saymyname.webapp.mapper.tenant;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.tenant.Tenant;
import com.saymyname.core.model.tenant.TenantOrg;
import com.saymyname.core.model.tenant.TenantPersonal;
import com.saymyname.webapp.dto.tenant.TenantDto;

@Component
public class TenantDtoMapper {

    public TenantDto toDto(Tenant tenant) {
        if (tenant == null) {
            return null;
        }

        if (tenant instanceof TenantOrg org) {
            return new TenantDto(
                    org.getId(),
                    org.getKind(),
                    org.getKey(),
                    org.getName(),
                    org.isActive(),
                    org.getCreatedAt(),
                    org.getUpdatedAt());
        }

        if (tenant instanceof TenantPersonal personal) {
            return new TenantDto(
                    personal.getId(),
                    personal.getKind(),
                    null,
                    null,
                    null,
                    personal.getCreatedAt(),
                    null);
        }

        throw new IllegalArgumentException("Unsupported tenant subtype: " + tenant.getClass().getName());
    }

    public Tenant toModel(TenantDto dto) {
        if (dto == null) {
            return null;
        }

        if (dto.kind() == null) {
            throw new IllegalArgumentException("Tenant kind is required");
        }

        return switch (dto.kind()) {
            case ORG -> TenantOrg.builder()
                    .id(dto.id())
                    .key(dto.key())
                    .name(dto.name())
                    .active(dto.active() != null ? dto.active() : true)
                    .createdAt(dto.createdAt())
                    .updatedAt(dto.updatedAt())
                    .build();

            case PERSONAL -> TenantPersonal.builder()
                    .id(dto.id())
                    .createdAt(dto.createdAt())
                    .build();
        };
    }
}