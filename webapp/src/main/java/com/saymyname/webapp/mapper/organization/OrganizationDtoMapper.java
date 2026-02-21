package com.saymyname.webapp.mapper.organization;

import com.saymyname.core.model.organization.TenantOrg;
import com.saymyname.webapp.dto.organization.OrganizationDto;

import org.springframework.stereotype.Component;

@Component
public class OrganizationDtoMapper {

    public OrganizationDto toDto(TenantOrg org) {
        if (org == null)
            return null;
        return new OrganizationDto(
                org.getId(),
                org.getKey(),
                org.getName());
    }

    public TenantOrg toModel(OrganizationDto dto) {
        if (dto == null)
            return null;
        return TenantOrg.builder()
                .id(dto.id())
                .key(dto.key())
                .name(dto.name())
                .active(true) // ou à adapter selon besoin
                .build();
    }
}
