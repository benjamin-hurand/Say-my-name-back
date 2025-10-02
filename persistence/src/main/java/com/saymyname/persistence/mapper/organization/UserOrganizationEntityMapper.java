package com.saymyname.persistence.mapper.organization;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.organization.UserOrganization;
import com.saymyname.persistence.entity.organization.UserOrganizationEntity;

@Component
public class UserOrganizationEntityMapper {

    private final OrganizationEntityMapper organizationMapper;

    public UserOrganizationEntityMapper(OrganizationEntityMapper organizationMapper) {
        this.organizationMapper = organizationMapper;
    }

    public UserOrganization toModel(UserOrganizationEntity entity) {
        if (entity == null)
            return null;
        return UserOrganization.builder()
                .userId(entity.getId().getUserId())
                .organizationId(entity.getId().getOrganizationId())
                .role(entity.getRole())
                .createdAt(entity.getCreatedAt())
                .organization(organizationMapper.toModel(entity.getOrganization()))
                .build();
    }

    public UserOrganizationEntity toEntity(UserOrganization model) {
        if (model == null)
            return null;
        return new UserOrganizationEntity(
                new com.saymyname.persistence.entity.organization.UserOrganizationId(
                        model.getUserId(),
                        model.getOrganizationId()),
                organizationMapper.toEntity(model.getOrganization()),
                model.getRole(),
                model.getCreatedAt());
    }
}
