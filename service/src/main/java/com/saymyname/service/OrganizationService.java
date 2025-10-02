// service/src/main/java/com/saymyname/service/OrganizationService.java
package com.saymyname.service;

import com.saymyname.core.model.organization.Organization;
import com.saymyname.persistence.dao.organization.OrganizationDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class OrganizationService {

    private final OrganizationDao dao;

    public OrganizationService(OrganizationDao dao) {
        this.dao = dao;
    }

    // CRUD si besoin
    public Organization create(String key, String name, boolean active) {
        return dao.create(Organization.builder()
                .key(key).name(name).active(active).build());
    }

    public Organization update(Organization org) {
        return dao.update(org);
    }

    public Organization getById(Long id) {
        return dao.getById(id);
    }

    public Organization getByKey(String key) {
        return dao.getByKey(key);
    }

    public List<Organization> listActiveOrganizations() {
        return dao.findActive();
    }

    public List<Long> listActiveOrganizationIds() {
        return dao.findActiveIds();
    }
}
