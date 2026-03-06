package com.saymyname.persistence.mapper.tenant;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.PersonLinkStatus;
import com.saymyname.core.model.tenant.TenantMembership;
import com.saymyname.persistence.entity.TenantEntity;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.PersonEntity;
import com.saymyname.persistence.entity.organization.TenantMembershipEntity;
import com.saymyname.persistence.entity.organization.UserTenantId;

@Component
public class TenantMembershipEntityMapper {

    private final TenantEntityMapper tenantMapper;

    public TenantMembershipEntityMapper(TenantEntityMapper tenantMapper) {
        this.tenantMapper = tenantMapper;
    }

    public TenantMembership toModel(TenantMembershipEntity entity) {
        if (entity == null) {
            return null;
        }

        Long userId = entity.getUser() != null
                ? entity.getUser().getId()
                : (entity.getId() != null ? entity.getId().getUserId() : null);

        Long tenantId = entity.getTenant() != null
                ? entity.getTenant().getId()
                : (entity.getId() != null ? entity.getId().getTenantId() : null);

        Long personId = entity.getPerson() != null ? entity.getPerson().getId() : null;

        return TenantMembership.builder()
                .userId(userId)
                .tenantId(tenantId)
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
                .tenant(entity.getTenant() != null ? tenantMapper.toModel(entity.getTenant()) : null)
                .build();
    }

    public TenantMembership toModelLight(TenantMembershipEntity entity) {
        if (entity == null) {
            return null;
        }

        Long userId = entity.getUser() != null
                ? entity.getUser().getId()
                : (entity.getId() != null ? entity.getId().getUserId() : null);

        Long tenantId = entity.getTenant() != null
                ? entity.getTenant().getId()
                : (entity.getId() != null ? entity.getId().getTenantId() : null);

        Long personId = entity.getPerson() != null ? entity.getPerson().getId() : null;

        return TenantMembership.builder()
                .userId(userId)
                .tenantId(tenantId)
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

    public TenantMembershipEntity toEntity(TenantMembership model) {
        if (model == null) {
            return null;
        }

        TenantMembershipEntity e = new TenantMembershipEntity();

        UserTenantId id = new UserTenantId(model.getUserId(), model.getTenantId());
        e.setId(id);

        e.setRole(model.getRole());
        e.setCreatedAt(model.getCreatedAt());
        e.setStatus(model.getStatus());

        e.setPersonLinkStatus(model.getPersonLinkStatus() != null
                ? model.getPersonLinkStatus()
                : PersonLinkStatus.NONE);
        e.setCanPickPerson(model.isCanPickPerson());
        e.setCanCreatePerson(model.isCanCreatePerson());
        e.setPickRequiresApproval(model.isPickRequiresApproval());
        e.setCreateRequiresApproval(model.isCreateRequiresApproval());

        if (model.getPersonId() != null) {
            PersonEntity p = new PersonEntity();
            p.setId(model.getPersonId());
            e.setPerson(p);
        } else {
            e.setPerson(null);
        }

        if (model.getTenant() != null) {
            e.setTenant(tenantMapper.toEntity(model.getTenant()));
        }

        return e;
    }

    public void applyToEntity(TenantMembership model, TenantMembershipEntity target) {
        if (model == null || target == null) {
            return;
        }

        if (model.getRole() != null) {
            target.setRole(model.getRole());
        }

        if (model.getCreatedAt() != null) {
            target.setCreatedAt(model.getCreatedAt());
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

        if (model.getTenant() != null) {
            target.setTenant(tenantMapper.toEntity(model.getTenant()));
        }
    }

    public void bindRefs(TenantMembershipEntity entity, UserEntity userRef, TenantEntity tenantRef) {
        if (entity == null) {
            return;
        }
        if (userRef != null) {
            entity.setUser(userRef);
        }
        if (tenantRef != null) {
            entity.setTenant(tenantRef);
        }
    }
}