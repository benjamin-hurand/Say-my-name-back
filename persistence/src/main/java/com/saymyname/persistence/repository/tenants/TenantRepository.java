package com.saymyname.persistence.repository.tenants;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saymyname.core.model.enums.tenant.TenantKind;
import com.saymyname.persistence.entity.TenantEntity;

@Repository
public interface TenantRepository extends JpaRepository<TenantEntity, Long> {

    List<TenantEntity> findByKind(TenantKind kind);
}