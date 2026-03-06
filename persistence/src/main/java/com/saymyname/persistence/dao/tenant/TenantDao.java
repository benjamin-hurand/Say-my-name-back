package com.saymyname.persistence.dao.tenant;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.enums.tenant.TenantKind;
import com.saymyname.core.model.tenant.Tenant;
import com.saymyname.persistence.entity.TenantEntity;
import com.saymyname.persistence.mapper.tenant.TenantEntityMapper;
import com.saymyname.persistence.repository.tenants.TenantRepository;

@Repository
@Transactional
public class TenantDao {

    private final TenantRepository repo;
    private final TenantEntityMapper mapper;

    public TenantDao(TenantRepository repo, TenantEntityMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    public Tenant getById(Long id) {
        TenantEntity entity = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Tenant not found: id=" + id));
        return mapper.toModel(entity);
    }

    public Optional<Tenant> findById(Long id) {
        return repo.findById(id).map(mapper::toModel);
    }

    public List<Tenant> findByKind(TenantKind kind) {
        return repo.findByKind(kind).stream()
                .map(mapper::toModel)
                .toList();
    }

    public boolean existsById(Long id) {
        return repo.existsById(id);
    }
}