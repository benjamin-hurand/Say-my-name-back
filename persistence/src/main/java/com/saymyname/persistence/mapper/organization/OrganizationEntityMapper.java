// persistence/src/main/java/com/saymyname/persistence/mapper/organization/OrganizationEntityMapper.java
package com.saymyname.persistence.mapper.organization;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.OrgType;
import com.saymyname.core.model.organization.Organization;
import com.saymyname.persistence.entity.organization.OrganizationEntity;

@Component
public class OrganizationEntityMapper {

    public Organization toModel(OrganizationEntity e) {
        if (e == null)
            return null;
        return Organization.builder()
                .id(e.getTenantId())
                .orgKey(e.getOrgKey())
                .name(e.getName())
                .orgType(e.getOrgType())
                .active(e.isActive())
                .createdAt(toInstant(e.getCreatedAt()))
                .updatedAt(toInstant(e.getUpdatedAt()))
                .build();
    }

    public OrganizationEntity toEntity(Organization m) {
        if (m == null)
            return null;
        OrganizationEntity e = OrganizationEntity.builder().build();
        e.setTenantId(m.getId());
        e.setOrgKey(m.getOrgKey());
        e.setName(m.getName());
        e.setOrgType(m.getOrgType());
        e.setActive(m.isActive());
        e.setCreatedAt(toLocalDateTime(m.getCreatedAt()));
        e.setUpdatedAt(toLocalDateTime(m.getUpdatedAt()));
        return e;
    }

    public void mergeIntoEntity(Organization m, OrganizationEntity e) {
        if (m == null || e == null)
            return;
        if (m.getOrgKey() != null)
            e.setOrgKey(m.getOrgKey());
        if (m.getName() != null)
            e.setName(m.getName());
        if (m.getOrgType() != null)
            e.setOrgType(m.getOrgType());
        e.setActive(m.isActive());
        // createdAt/updatedAt: managed by DB + lifecycle hooks.
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
