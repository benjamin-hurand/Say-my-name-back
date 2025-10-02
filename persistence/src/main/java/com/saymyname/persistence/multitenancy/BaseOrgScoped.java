package com.saymyname.persistence.multitenancy;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

/** A étendre par toutes les entités qui ont organization_id. */
@MappedSuperclass
@FilterDef(name = "orgFilter", parameters = @ParamDef(name = "orgId", type = Long.class))
@Filter(name = "orgFilter", condition = "organization_id = :orgId")
@EntityListeners(OrgFillListener.class)
public abstract class BaseOrgScoped implements HasOrganization {

    @Column(name = "organization_id", nullable = false)
    private Long organizationId;

    @Override
    public Long getOrganizationId() {
        return organizationId;
    }

    @Override
    public void setOrganizationId(Long id) {
        this.organizationId = id;
    }
}
