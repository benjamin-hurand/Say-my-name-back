package com.saymyname.service.tenant;

import com.saymyname.core.model.tenant.TenantOrg;
import com.saymyname.persistence.dao.tenant.TenantOrgDao;
import com.saymyname.service.AttributeService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TenantOrgService {

    private final TenantOrgDao dao;
    private final AttributeService attributeService;

    public TenantOrgService(TenantOrgDao dao, AttributeService attributeService) {
        this.dao = dao;
        this.attributeService = attributeService;
    }

    public TenantOrg create(String key, String name, boolean active) {
        TenantOrg tenant = dao.create(TenantOrg.builder()
                .key(key)
                .name(name)
                .active(active)
                .build());
        attributeService.provisionIdentityAttribute(tenant.getId());
        return tenant;
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
