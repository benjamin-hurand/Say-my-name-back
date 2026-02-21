// src/main/java/com/saymyname/persistence/mapper/organization/UserOrganizationEntityMapper.java
package com.saymyname.persistence.mapper.organization;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.PersonLinkStatus;
import com.saymyname.core.model.organization.UserOrganization;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.TenantOrgEntity;
import com.saymyname.persistence.entity.organization.PersonEntity;
import com.saymyname.persistence.entity.organization.UserOrganizationEntity;
import com.saymyname.persistence.entity.organization.UserOrganizationId;

@Component
public class UserOrganizationEntityMapper {

    private final TenantOrgEntityMapper organizationMapper;

    public UserOrganizationEntityMapper(TenantOrgEntityMapper organizationMapper) {
        this.organizationMapper = organizationMapper;
    }

    // -------------------------------------------------------------------------
    // Entity -> Model
    // -------------------------------------------------------------------------

    public UserOrganization toModel(UserOrganizationEntity entity) {
        if (entity == null) {
            return null;
        }

        Long userId = (entity.getUser() != null ? entity.getUser().getId()
                : (entity.getId() != null ? entity.getId().getUserId() : null));

        Long orgId = (entity.getOrganization() != null ? entity.getOrganization().getId()
                : (entity.getId() != null ? entity.getId().getOrganizationId() : null));

        Long personId = (entity.getPerson() != null ? entity.getPerson().getId() : null);

        return UserOrganization.builder()
                .userId(userId)
                .organizationId(orgId)
                .personId(personId)
                .role(entity.getRole())
                .createdAt(entity.getCreatedAt())
                .organization(organizationMapper.toModel(entity.getOrganization()))

                // membership status (accès)
                .status(entity.getStatus())

                // identity link + onboarding policy
                .personLinkStatus(entity.getPersonLinkStatus() != null
                        ? entity.getPersonLinkStatus()
                        : PersonLinkStatus.NONE)
                .canPickPerson(entity.isCanPickPerson())
                .canCreatePerson(entity.isCanCreatePerson())
                .pickRequiresApproval(entity.isPickRequiresApproval())
                .createRequiresApproval(entity.isCreateRequiresApproval())

                .build();
    }

    public UserOrganization toModelLight(UserOrganizationEntity entity) {
        if (entity == null) {
            return null;
        }

        Long userId = (entity.getUser() != null ? entity.getUser().getId()
                : (entity.getId() != null ? entity.getId().getUserId() : null));

        Long orgId = (entity.getOrganization() != null ? entity.getOrganization().getId()
                : (entity.getId() != null ? entity.getId().getOrganizationId() : null));

        Long personId = (entity.getPerson() != null ? entity.getPerson().getId() : null);

        return UserOrganization.builder()
                .userId(userId)
                .organizationId(orgId)
                .personId(personId)
                .role(entity.getRole())
                .createdAt(entity.getCreatedAt())
                .status(entity.getStatus())
                .personLinkStatus(entity.getPersonLinkStatus() != null
                        ? entity.getPersonLinkStatus()
                        : PersonLinkStatus.NONE)
                .canPickPerson(entity.isCanPickPerson())
                .canCreatePerson(entity.isCanCreatePerson())
                .pickRequiresApproval(entity.isPickRequiresApproval())
                .createRequiresApproval(entity.isCreateRequiresApproval())
                .build();
    }

    // -------------------------------------------------------------------------
    // Model -> Entity
    // -------------------------------------------------------------------------

    public UserOrganizationEntity toEntity(UserOrganization model) {
        if (model == null) {
            return null;
        }

        UserOrganizationEntity e = new UserOrganizationEntity();

        // EmbeddedId (PK composite)
        UserOrganizationId id = new UserOrganizationId(model.getUserId(), model.getOrganizationId());
        e.setId(id);

        // Champs simples
        e.setRole(model.getRole());

        // membership status
        e.setStatus(model.getStatus());

        // Person link + policy
        e.setPersonLinkStatus(model.getPersonLinkStatus() != null
                ? model.getPersonLinkStatus()
                : PersonLinkStatus.NONE);
        e.setCanPickPerson(model.isCanPickPerson());
        e.setCanCreatePerson(model.isCanCreatePerson());
        e.setPickRequiresApproval(model.isPickRequiresApproval());
        e.setCreateRequiresApproval(model.isCreateRequiresApproval());

        // Person : id-only
        if (model.getPersonId() != null) {
            PersonEntity p = new PersonEntity();
            p.setId(model.getPersonId()); // nécessite setId(Long) dans PersonEntity
            e.setPerson(p);
        } else {
            e.setPerson(null);
        }

        return e;
    }

    public void applyToEntity(UserOrganization model, UserOrganizationEntity target) {
        if (model == null || target == null) {
            return;
        }

        if (model.getRole() != null) {
            target.setRole(model.getRole());
        }

        if (model.getStatus() != null) {
            target.setStatus(model.getStatus());
        }

        target.setPersonLinkStatus(model.getPersonLinkStatus() != null
                ? model.getPersonLinkStatus()
                : PersonLinkStatus.NONE);
        target.setCanPickPerson(model.isCanPickPerson());
        target.setCanCreatePerson(model.isCanCreatePerson());
        target.setPickRequiresApproval(model.isPickRequiresApproval());
        target.setCreateRequiresApproval(model.isCreateRequiresApproval());

        if (model.getPersonId() != null) {
            PersonEntity p = new PersonEntity();
            p.setId(model.getPersonId());
            target.setPerson(p);
        } else {
            target.setPerson(null);
        }
    }

    public void bindRefs(UserOrganizationEntity entity, UserEntity userRef, TenantOrgEntity orgRef) {
        if (entity == null)
            return;
        if (userRef != null)
            entity.setUser(userRef);
        if (orgRef != null)
            entity.setOrganization(orgRef);
    }
}
