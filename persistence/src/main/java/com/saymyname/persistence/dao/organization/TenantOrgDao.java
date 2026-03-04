// persistence/src/main/java/com/saymyname/persistence/dao/organization/OrganizationDao.java
package com.saymyname.persistence.dao.organization;

import com.saymyname.core.model.organization.TenantOrg;
import com.saymyname.persistence.entity.organization.TenantOrgEntity;
import com.saymyname.persistence.mapper.organization.TenantOrgEntityMapper;
import com.saymyname.persistence.repository.TenantOrgRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Repository
@Transactional
public class TenantOrgDao {

    private final TenantOrgRepository repo;
    private final TenantOrgEntityMapper mapper;

    public TenantOrgDao(TenantOrgRepository repo, TenantOrgEntityMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public TenantOrg create(TenantOrg org) {
        TenantOrgEntity saved = repo.save(mapper.toEntity(org));
        return mapper.toModel(saved);
    }

    public TenantOrg update(TenantOrg org) {
        TenantOrgEntity e = repo.findById(org.getId())
                .orElseThrow(() -> new NoSuchElementException("Organization not found: id=" + org.getId()));
        mapper.mergeIntoEntity(org, e);
        return mapper.toModel(repo.save(e));
    }

    public TenantOrg getById(Long id) {
        return mapper.toModel(repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Organization not found: id=" + id)));
    }

    public TenantOrg getByKey(String key) {
        return mapper.toModel(repo.findByOrgKey(key)
                .orElseThrow(() -> new NoSuchElementException("Organization not found: key=" + key)));
    }

    public List<TenantOrg> findActive() {
        return repo.findByActiveTrue().stream().map(mapper::toModel).toList();
    }

    public List<Long> findActiveIds() {
        return repo.findActiveTenantOrgIds();
    }
}
