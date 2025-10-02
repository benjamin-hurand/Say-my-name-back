// persistence/src/main/java/com/saymyname/persistence/mapper/organization/OrganizationEntityMapper.java
package com.saymyname.persistence.mapper.organization;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.organization.Organization;
import com.saymyname.persistence.entity.organization.OrganizationEntity;

@Component
public class OrganizationEntityMapper {

    public Organization toModel(OrganizationEntity e) {
        if (e == null)
            return null;
        return Organization.builder()
                .id(e.getId())
                .key(e.getKey())
                .name(e.getName())
                .active(e.isActive())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }

    public OrganizationEntity toEntity(Organization m) {
        if (m == null)
            return null;
        OrganizationEntity e = new OrganizationEntity();
        e.setId(m.getId());
        e.setKey(m.getKey());
        e.setName(m.getName());
        e.setActive(m.isActive());
        e.setCreatedAt(m.getCreatedAt());
        e.setUpdatedAt(m.getUpdatedAt());
        return e;
    }

    public void mergeIntoEntity(Organization m, OrganizationEntity e) {
        if (m == null || e == null)
            return;
        if (m.getKey() != null)
            e.setKey(m.getKey());
        if (m.getName() != null)
            e.setName(m.getName());
        e.setActive(m.isActive());
        // createdAt/updatedAt: laissés au SGBD + lifecycle hooks
    }
}
