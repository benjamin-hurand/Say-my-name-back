package com.saymyname.webapp.mapper.organization;

import com.saymyname.core.model.organization.UserOrganization;
import com.saymyname.webapp.dto.organization.OrganizationDto;
import com.saymyname.webapp.dto.organization.UserOrganizationDto;

import org.springframework.stereotype.Component;

@Component
public class UserOrganizationDtoMapper {

    private final OrganizationDtoMapper organizationMapper;

    public UserOrganizationDtoMapper(OrganizationDtoMapper organizationMapper) {
        this.organizationMapper = organizationMapper;
    }

    public UserOrganizationDto toDto(UserOrganization uo) {
        if (uo == null)
            return null;
        return new UserOrganizationDto(
                uo.getOrganizationId(),
                uo.getOrganization() != null ? uo.getOrganization().getKey() : null,
                uo.getOrganization() != null ? uo.getOrganization().getName() : null,
                uo.getRole(),
                uo.getCreatedAt());
    }

    public UserOrganization toModel(UserOrganizationDto dto) {
        if (dto == null)
            return null;
        return UserOrganization.builder()
                .organizationId(dto.organizationId())
                .organization(organizationMapper.toModel(
                        new OrganizationDto(dto.organizationId(), dto.organizationKey(), dto.organizationName())))
                .role(dto.role())
                .createdAt(dto.createdAt())
                .build();
    }
}
