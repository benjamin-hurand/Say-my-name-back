package com.saymyname.persistence.mapper.tenant;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.tenant.Tenant;
import com.saymyname.persistence.entity.TenantEntity;
import com.saymyname.persistence.entity.TenantPersonalEntity;
import com.saymyname.persistence.entity.organization.TenantOrgEntity;

@Component
public class TenantEntityMapper {

    private final TenantOrgEntityMapper tenantOrgEntityMapper;
    private final TenantPersonalEntityMapper tenantPersonalEntityMapper;

    public TenantEntityMapper(
            TenantOrgEntityMapper tenantOrgEntityMapper,
            TenantPersonalEntityMapper tenantPersonalEntityMapper) {
        this.tenantOrgEntityMapper = tenantOrgEntityMapper;
        this.tenantPersonalEntityMapper = tenantPersonalEntityMapper;
    }

    public Tenant toModel(TenantEntity e) {
        if (e == null) {
            return null;
        }

        if (e instanceof TenantOrgEntity orgEntity) {
            return tenantOrgEntityMapper.toModel(orgEntity);
        }

        if (e instanceof TenantPersonalEntity personalEntity) {
            return tenantPersonalEntityMapper.toModel(personalEntity);
        }

        throw new IllegalArgumentException(
                "Unsupported TenantEntity subtype: " + e.getClass().getName());
    }

    public TenantEntity toEntity(Tenant m) {
        if (m == null) {
            return null;
        }

        if (m instanceof com.saymyname.core.model.tenant.TenantOrg org) {
            return tenantOrgEntityMapper.toEntity(org);
        }

        if (m instanceof com.saymyname.core.model.tenant.TenantPersonal personal) {
            return tenantPersonalEntityMapper.toEntity(personal);
        }

        throw new IllegalArgumentException(
                "Unsupported Tenant model subtype: " + m.getClass().getName());
    }

    public void mergeIntoEntity(Tenant m, TenantEntity e) {
        if (m == null || e == null) {
            return;
        }

        if (m instanceof com.saymyname.core.model.tenant.TenantOrg org
                && e instanceof TenantOrgEntity orgEntity) {
            tenantOrgEntityMapper.mergeIntoEntity(org, orgEntity);
            return;
        }

        if (m instanceof com.saymyname.core.model.tenant.TenantPersonal personal
                && e instanceof TenantPersonalEntity personalEntity) {
            tenantPersonalEntityMapper.mergeIntoEntity(personal, personalEntity);
            return;
        }

        throw new IllegalArgumentException(
                "Incompatible tenant/model types: model=" + m.getClass().getName()
                        + ", entity=" + e.getClass().getName());
    }
}