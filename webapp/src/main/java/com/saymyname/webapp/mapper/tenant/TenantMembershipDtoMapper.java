package com.saymyname.webapp.mapper.tenant;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.tenant.TenantMembership;
import com.saymyname.webapp.dto.tenant.TenantMembershipDto;

@Component
public class TenantMembershipDtoMapper {

    private final TenantDtoMapper tenantDtoMapper;

    public TenantMembershipDtoMapper(TenantDtoMapper tenantDtoMapper) {
        this.tenantDtoMapper = tenantDtoMapper;
    }

    public TenantMembershipDto toDto(TenantMembership membership) {
        if (membership == null) {
            return null;
        }

        return new TenantMembershipDto(
                membership.getTenantId(),
                membership.getRole(),
                membership.getCreatedAt(),
                tenantDtoMapper.toDto(membership.getTenant()));
    }

    public TenantMembership toModel(TenantMembershipDto dto) {
        if (dto == null) {
            return null;
        }

        return TenantMembership.builder()
                .tenantId(dto.tenantId())
                .role(dto.role())
                .createdAt(dto.createdAt())
                .tenant(tenantDtoMapper.toModel(dto.tenant()))
                .build();
    }
}