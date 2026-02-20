package com.saymyname.persistence.multitenancy;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = Long.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@EntityListeners(TenantFillListener.class)
public abstract class BaseTenantScoped implements HasTenant {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Override
    public Long getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(Long id) {
        this.tenantId = id;
    }

    // Backward-compatible aliases for legacy code still using organization naming.
    public Long getOrganizationId() {
        return tenantId;
    }

    public void setOrganizationId(Long organizationId) {
        this.tenantId = organizationId;
    }
}
