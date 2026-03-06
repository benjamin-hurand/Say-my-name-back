package com.saymyname.persistence.mapper.tenant;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.tenant.TenantPersonal;
import com.saymyname.persistence.entity.TenantPersonalEntity;
import com.saymyname.persistence.entity.UserEntity;

@Component
public class TenantPersonalEntityMapper {

    public TenantPersonal toModel(TenantPersonalEntity e) {
        if (e == null) {
            return null;
        }

        Long ownerUserId = e.getOwnerUser() != null ? e.getOwnerUser().getId() : null;

        return TenantPersonal.builder()
                .id(e.getId())
                .ownerUserId(ownerUserId)
                .createdAt(e.getCreatedAt())
                .build();
    }

    public TenantPersonalEntity toEntity(TenantPersonal m) {
        if (m == null) {
            return null;
        }

        TenantPersonalEntity e = new TenantPersonalEntity();
        e.setId(m.getId());

        if (m.getOwnerUserId() != null) {
            UserEntity owner = new UserEntity();
            owner.setId(m.getOwnerUserId());
            e.setOwnerUser(owner);
        }

        return e;
    }

    public void mergeIntoEntity(TenantPersonal m, TenantPersonalEntity e) {
        if (m == null || e == null) {
            return;
        }

        if (m.getOwnerUserId() != null) {
            UserEntity owner = new UserEntity();
            owner.setId(m.getOwnerUserId());
            e.setOwnerUser(owner);
        }
    }
}