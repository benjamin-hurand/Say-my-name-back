package com.saymyname.service.tenant;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.tenant.TenantPersonal;
import com.saymyname.persistence.dao.tenant.TenantPersonalDao;

@Service
@Transactional(readOnly = true)
public class TenantPersonalService {

    private final TenantPersonalDao dao;

    public TenantPersonalService(TenantPersonalDao dao) {
        this.dao = dao;
    }

    public TenantPersonal getById(Long id) {
        return dao.getById(id);
    }

    public Optional<TenantPersonal> findById(Long id) {
        return dao.findById(id);
    }

    public Optional<TenantPersonal> findByOwnerUserId(Long ownerUserId) {
        return dao.findByOwnerUserId(ownerUserId);
    }

    public boolean existsByOwnerUserId(Long ownerUserId) {
        return dao.existsByOwnerUserId(ownerUserId);
    }
}