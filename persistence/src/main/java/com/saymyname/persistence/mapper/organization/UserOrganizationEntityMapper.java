// src/main/java/com/saymyname/persistence/mapper/organization/UserOrganizationEntityMapper.java
package com.saymyname.persistence.mapper.organization;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.MembershipStatus;
import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.model.enums.PersonLinkStatus;
import com.saymyname.core.model.organization.UserOrganization;
import com.saymyname.persistence.entity.UserEmailEntity;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.OrganizationEntity;
import com.saymyname.persistence.entity.organization.UserOrganizationEntity;
import com.saymyname.persistence.entity.organization.UserOrganizationId;

@Component
public class UserOrganizationEntityMapper {

    private final OrganizationEntityMapper organizationMapper;

    public UserOrganizationEntityMapper(OrganizationEntityMapper organizationMapper) {
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

        Long orgId = (entity.getOrganization() != null ? entity.getOrganization().getTenantId()
                : (entity.getId() != null ? entity.getId().getTenantId() : null));

        Long preferredEmailId = entity.getPreferredEmail() != null ? entity.getPreferredEmail().getId() : null;

        return UserOrganization.builder()
                .userId(userId)
                .organizationId(orgId)
                .role(toModelRole(entity.getRole()))
                .displayName(entity.getDisplayName())
                .createdAt(toInstant(entity.getCreatedAt()))
                .personId(entity.getPersonId())
                .status(toModelStatus(entity.getStatus()))
                .personLinkStatus(entity.getPersonLinkStatus() != null
                        ? toModelPersonLinkStatus(entity.getPersonLinkStatus())
                        : PersonLinkStatus.NONE)
                .canPickPerson(entity.isCanPickPerson())
                .canCreatePerson(entity.isCanCreatePerson())
                .pickRequiresApproval(entity.isPickRequiresApproval())
                .createRequiresApproval(entity.isCreateRequiresApproval())
                .preferredEmailId(preferredEmailId)
                .build();
    }

    public UserOrganization toModelLight(UserOrganizationEntity entity) {
        return toModel(entity);
    }

    // -------------------------------------------------------------------------
    // Model -> Entity
    // -------------------------------------------------------------------------

    public UserOrganizationEntity toEntity(UserOrganization model) {
        if (model == null) {
            return null;
        }

        UserOrganizationEntity e = UserOrganizationEntity.builder().build();

        // EmbeddedId (PK composite)
        if (model.getUserId() != null && model.getOrganizationId() != null) {
            UserOrganizationId id = new UserOrganizationId(model.getOrganizationId(), model.getUserId());
            e.setId(id);
        }

        // Champs simples
        e.setRole(toEntityRole(model.getRole()));
        e.setDisplayName(model.getDisplayName());
        e.setCreatedAt(toLocalDateTime(model.getCreatedAt()));

        // membership status
        e.setStatus(toEntityStatus(model.getStatus()));

        // Person link + policy
        e.setPersonId(model.getPersonId());
        e.setPersonLinkStatus(model.getPersonLinkStatus() != null
                ? toEntityPersonLinkStatus(model.getPersonLinkStatus())
                : UserOrganizationEntity.PersonLinkStatus.NONE);
        e.setCanPickPerson(model.isCanPickPerson());
        e.setCanCreatePerson(model.isCanCreatePerson());
        e.setPickRequiresApproval(model.isPickRequiresApproval());
        e.setCreateRequiresApproval(model.isCreateRequiresApproval());

        if (model.getPreferredEmailId() != null) {
            e.setPreferredEmail(UserEmailEntity.builder().id(model.getPreferredEmailId()).build());
        } else {
            e.setPreferredEmail(null);
        }

        return e;
    }

    public void applyToEntity(UserOrganization model, UserOrganizationEntity target) {
        if (model == null || target == null) {
            return;
        }

        if (model.getRole() != null) {
            target.setRole(toEntityRole(model.getRole()));
        }

        if (model.getDisplayName() != null) {
            target.setDisplayName(model.getDisplayName());
        }

        if (model.getStatus() != null) {
            target.setStatus(toEntityStatus(model.getStatus()));
        }

        target.setPersonId(model.getPersonId());
        target.setPersonLinkStatus(model.getPersonLinkStatus() != null
                ? toEntityPersonLinkStatus(model.getPersonLinkStatus())
                : UserOrganizationEntity.PersonLinkStatus.NONE);
        target.setCanPickPerson(model.isCanPickPerson());
        target.setCanCreatePerson(model.isCanCreatePerson());
        target.setPickRequiresApproval(model.isPickRequiresApproval());
        target.setCreateRequiresApproval(model.isCreateRequiresApproval());

        if (model.getPreferredEmailId() != null) {
            target.setPreferredEmail(UserEmailEntity.builder().id(model.getPreferredEmailId()).build());
        } else {
            target.setPreferredEmail(null);
        }
    }

    public void bindRefs(UserOrganizationEntity entity, UserEntity userRef, OrganizationEntity orgRef) {
        if (entity == null)
            return;
        if (userRef != null)
            entity.setUser(userRef);
        if (orgRef != null)
            entity.setOrganization(orgRef);
    }

    private OrgRole toModelRole(UserOrganizationEntity.MemberRole value) {
        return value == null ? null : OrgRole.valueOf(value.name());
    }

    private UserOrganizationEntity.MemberRole toEntityRole(OrgRole value) {
        return value == null ? null : UserOrganizationEntity.MemberRole.valueOf(value.name());
    }

    private MembershipStatus toModelStatus(UserOrganizationEntity.MemberStatus value) {
        return value == null ? null : MembershipStatus.valueOf(value.name());
    }

    private UserOrganizationEntity.MemberStatus toEntityStatus(MembershipStatus value) {
        return value == null ? null : UserOrganizationEntity.MemberStatus.valueOf(value.name());
    }

    private PersonLinkStatus toModelPersonLinkStatus(UserOrganizationEntity.PersonLinkStatus value) {
        return value == null ? null : PersonLinkStatus.valueOf(value.name());
    }

    private UserOrganizationEntity.PersonLinkStatus toEntityPersonLinkStatus(PersonLinkStatus value) {
        return value == null ? null : UserOrganizationEntity.PersonLinkStatus.valueOf(value.name());
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
