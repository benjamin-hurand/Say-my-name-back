package com.saymyname.service.tenant;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.enums.tenant.TenantKind;
import com.saymyname.core.model.tenant.Tenant;
import com.saymyname.persistence.dao.tenant.TenantDao;

@Service
@Transactional(readOnly = true)
public class TenantService {

    private final TenantDao dao;

    public TenantService(TenantDao dao) {
        this.dao = dao;
    }

    public Tenant getById(Long id) {
        return dao.getById(id);
    }

    public Optional<Tenant> findById(Long id) {
        return dao.findById(id);
    }

    public List<Tenant> findByKind(TenantKind kind) {
        return dao.findByKind(kind);
    }

    public boolean existsById(Long id) {
        return dao.existsById(id);
    }
}