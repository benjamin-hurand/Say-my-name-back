// persistence/src/main/java/com/saymyname/persistence/mapper/organization/OrganizationEntityMapper.java
package com.saymyname.persistence.mapper.organization;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.organization.TenantOrg;
import com.saymyname.persistence.entity.organization.TenantOrgEntity;

@Component
public class TenantOrgEntityMapper {

    public TenantOrg toModel(TenantOrgEntity e) {
        if (e == null)
            return null;
        return TenantOrg.builder()
                .id(e.getId())
                .key(e.getOrgKey())
                .name(e.getName())
                .active(e.isActive())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public TenantOrgEntity toEntity(TenantOrg m) {
        if (m == null)
            return null;
        TenantOrgEntity e = new TenantOrgEntity();
        e.setId(m.getId());
        e.setOrgKey(m.getKey());
        e.setName(m.getName());
        e.setActive(m.isActive());
        e.setCreatedAt(m.getCreatedAt());
        e.setUpdatedAt(m.getUpdatedAt());
        return e;
    }

    public void mergeIntoEntity(TenantOrg m, TenantOrgEntity e) {
        if (m == null || e == null)
            return;
        if (m.getKey() != null)
            e.setOrgKey(m.getKey());
        if (m.getName() != null)
            e.setName(m.getName());
        e.setActive(m.isActive());
        // createdAt/updatedAt: laissés au SGBD + lifecycle hooks
    }
}
