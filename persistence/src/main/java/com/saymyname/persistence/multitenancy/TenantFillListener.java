package com.saymyname.persistence.multitenancy;

import com.saymyname.core.multitenancy.TenantContext;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class TenantFillListener {

    @PrePersist
    @PreUpdate
    public void fillTenantId(HasTenant entity) {
        if (entity.getTenantId() == null) {
            Long tenantId = TenantContext.get();
            if (tenantId == null) {
                throw new IllegalStateException("Cannot persist/update tenant-scoped entity without tenant_id in TenantContext: " + entity.getClass().getName());
            }
            entity.setTenantId(tenantId);
        }
    }
}