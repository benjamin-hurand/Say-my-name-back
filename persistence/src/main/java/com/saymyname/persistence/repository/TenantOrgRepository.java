// persistence/src/main/java/com/saymyname/persistence/repository/OrganizationRepository.java
package com.saymyname.persistence.repository;

import com.saymyname.persistence.entity.organization.TenantOrgEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TenantOrgRepository extends JpaRepository<TenantOrgEntity, Long> {

    Optional<TenantOrgEntity> findByKey(String key);

    List<TenantOrgEntity> findByActiveTrue();

    @Query("select o.id from TenantOrgEntity o where o.active = true")
    List<Long> findActiveTenantOrgIds();

    @Query("select o.key from TenantOrgEntity o where o.id = :orgId")
    Optional<String> findKeyById(@Param("orgId") Long orgId);
}
