package com.saymyname.persistence.mapper.organization;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.organization.UserOrganization;
import com.saymyname.persistence.entity.organization.UserOrganizationEntity;
import com.saymyname.persistence.entity.organization.UserOrganizationId;

@Component
public class UserOrganizationEntityMapper {

    private final OrganizationEntityMapper organizationMapper;

    public UserOrganizationEntityMapper(OrganizationEntityMapper organizationMapper) {
        this.organizationMapper = organizationMapper;
    }

    public UserOrganization toModel(UserOrganizationEntity entity) {
        if (entity == null) {
            return null;
        }

        return UserOrganization.builder()
                .userId(entity.getId().getUserId())
                .organizationId(entity.getId().getOrganizationId())
                .personId(entity.getPersonId()) // nouveau champ
                .role(entity.getRole())
                .createdAt(entity.getCreatedAt())
                .organization(organizationMapper.toModel(entity.getOrganization()))
                .build();
    }

    public UserOrganizationEntity toEntity(UserOrganization model) {
        if (model == null) {
            return null;
        }

        UserOrganizationId id = new UserOrganizationId(
                model.getUserId(),
                model.getOrganizationId());

        return new UserOrganizationEntity(
                id,
                organizationMapper.toEntity(model.getOrganization()),
                model.getRole(),
                model.getCreatedAt(),
                model.getPersonId() // nouveau champ
        );
    }
}
