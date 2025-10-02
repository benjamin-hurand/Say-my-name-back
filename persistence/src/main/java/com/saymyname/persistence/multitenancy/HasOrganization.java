package com.saymyname.persistence.multitenancy;

public interface HasOrganization {
    Long getOrganizationId();

    void setOrganizationId(Long id);
}