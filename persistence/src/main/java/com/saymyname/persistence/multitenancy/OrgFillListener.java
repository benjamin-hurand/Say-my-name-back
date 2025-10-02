package com.saymyname.persistence.multitenancy;

import com.saymyname.core.multitenancy.OrgContext;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class OrgFillListener {

    @PrePersist
    @PreUpdate
    public void ensureOrgId(HasOrganization entity) {
        if (entity.getOrganizationId() == null) {
            Long currentOrg = OrgContext.get();
            if (currentOrg == null) {
                throw new IllegalStateException(
                        "Impossible de persister une entité sans organization_id (OrgContext est null): "
                                + entity.getClass().getSimpleName());
            }
            entity.setOrganizationId(currentOrg);
        }
    }
}
