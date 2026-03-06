package com.saymyname.persistence.multitenancy;

import com.saymyname.core.multitenancy.TenantContext;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class TenantFillListener {

    @PrePersist
    @PreUpdate
    public void ensureTenantId(HasTenant entity) {
        if (entity.getTenantId() == null) {
            Long currentTenant = TenantContext.get();
            if (currentTenant == null) {
                throw new IllegalStateException(
                        "Impossible de persister une entite sans tenant_id (TenantContext est null): "
                                + entity.getClass().getSimpleName());
            }
            entity.setTenantId(currentTenant);
        }
    }
}
