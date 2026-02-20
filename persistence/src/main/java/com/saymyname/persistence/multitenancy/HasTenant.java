package com.saymyname.persistence.multitenancy;

public interface HasTenant {
    Long getTenantId();

    void setTenantId(Long id);
}
