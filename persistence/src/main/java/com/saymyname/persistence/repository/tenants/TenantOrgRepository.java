package com.saymyname.persistence.repository.tenants;

import com.saymyname.persistence.entity.organization.TenantOrgEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TenantOrgRepository extends JpaRepository<TenantOrgEntity, Long> {

        Optional<TenantOrgEntity> findByOrgKey(String orgKey);

        @Query("select o.id from TenantOrgEntity o where o.active = true")
        List<Long> findActiveTenantOrgIds();

        @Query("select o.orgKey from TenantOrgEntity o where o.id = :tenantId")
        Optional<String> findOrgKeyByTenantId(@Param("tenantId") Long tenantId);

        List<TenantOrgEntity> findByActiveTrue();
}