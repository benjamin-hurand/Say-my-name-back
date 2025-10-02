// persistence/src/main/java/com/saymyname/persistence/dao/organization/OrganizationDao.java
package com.saymyname.persistence.dao.organization;

import com.saymyname.core.model.organization.Organization;
import com.saymyname.persistence.entity.organization.OrganizationEntity;
import com.saymyname.persistence.mapper.organization.OrganizationEntityMapper;
import com.saymyname.persistence.repository.OrganizationRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Repository
@Transactional
public class OrganizationDao {

    private final OrganizationRepository repo;
    private final OrganizationEntityMapper mapper;

    public OrganizationDao(OrganizationRepository repo, OrganizationEntityMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public Organization create(Organization org) {
        OrganizationEntity saved = repo.save(mapper.toEntity(org));
        return mapper.toModel(saved);
    }

    public Organization update(Organization org) {
        OrganizationEntity e = repo.findById(org.getId())
                .orElseThrow(() -> new NoSuchElementException("Organization not found: id=" + org.getId()));
        mapper.mergeIntoEntity(org, e);
        return mapper.toModel(repo.save(e));
    }

    public Organization getById(Long id) {
        return mapper.toModel(repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Organization not found: id=" + id)));
    }

    public Organization getByKey(String key) {
        return mapper.toModel(repo.findByKey(key)
                .orElseThrow(() -> new NoSuchElementException("Organization not found: key=" + key)));
    }

    public List<Organization> findActive() {
        return repo.findByActiveTrue().stream().map(mapper::toModel).toList();
    }

    public List<Long> findActiveIds() {
        return repo.findActiveOrganizationIds();
    }
}
