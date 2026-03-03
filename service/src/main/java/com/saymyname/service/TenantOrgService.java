// service/src/main/java/com/saymyname/service/TenantOrgService.java
package com.saymyname.service;

import com.saymyname.core.model.organization.TenantOrg;
import com.saymyname.persistence.dao.organization.TenantOrgDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TenantOrgService {

    private final TenantOrgDao dao;

    public TenantOrgService(TenantOrgDao dao) {
        this.dao = dao;
    }

    // CRUD si besoin
    public TenantOrg create(String key, String name, boolean active) {
        return dao.create(TenantOrg.builder()
                .key(key).name(name).active(active).build());
    }

    public TenantOrg update(TenantOrg org) {
        return dao.update(org);
    }

    public TenantOrg getById(Long id) {
        return dao.getById(id);
    }

    public TenantOrg getByKey(String key) {
        return dao.getByKey(key);
    }

    public List<TenantOrg> listActiveTenantOrgs() {
        return dao.findActive();
    }

    public List<Long> listActiveTenantOrgIds() {
        return dao.findActiveIds();
    }
}
