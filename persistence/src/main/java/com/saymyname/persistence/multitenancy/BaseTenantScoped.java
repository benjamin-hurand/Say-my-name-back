package com.saymyname.persistence.multitenancy;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@ToString(onlyExplicitlyIncluded = true)
@MappedSuperclass
@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = long.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@EntityListeners(TenantFillListener.class)
public abstract class BaseTenantScoped implements HasTenant {

    @ToString.Include
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    // Backward-compatible alias used by older DAO/repository code paths.
    public Long getOrganizationId() {
        return tenantId;
    }

    // Backward-compatible alias used by older DAO/repository code paths.
    public void setOrganizationId(Long organizationId) {
        this.tenantId = organizationId;
    }
}
