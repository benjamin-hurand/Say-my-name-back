package com.saymyname.persistence.dao.tenant;

import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.tenant.TenantPersonal;
import com.saymyname.persistence.entity.TenantPersonalEntity;
import com.saymyname.persistence.mapper.tenant.TenantPersonalEntityMapper;
import com.saymyname.persistence.repository.tenants.TenantPersonalRepository;

@Repository
@Transactional
public class TenantPersonalDao {

    private final TenantPersonalRepository repo;
    private final TenantPersonalEntityMapper mapper;

    public TenantPersonalDao(TenantPersonalRepository repo, TenantPersonalEntityMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public TenantPersonal getById(Long id) {
        TenantPersonalEntity entity = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tenant personal not found: id=" + id));
        return mapper.toModel(entity);
    }

    public Optional<TenantPersonal> findById(Long id) {
        return repo.findById(id).map(mapper::toModel);
    }

    public Optional<TenantPersonal> findByOwnerUserId(Long ownerUserId) {
        return repo.findByOwnerUser_Id(ownerUserId).map(mapper::toModel);
    }

    public boolean existsByOwnerUserId(Long ownerUserId) {
        return repo.existsByOwnerUser_Id(ownerUserId);
    }
}