// persistence/src/main/java/com/saymyname/persistence/repository/OrganizationRepository.java
package com.saymyname.persistence.repository;

import com.saymyname.persistence.entity.organization.OrganizationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Long> {

    Optional<OrganizationEntity> findByKey(String key);

    List<OrganizationEntity> findByActiveTrue();

    @Query("select o.id from OrganizationEntity o where o.active = true")
    List<Long> findActiveOrganizationIds();

    @Query("select o.key from OrganizationEntity o where o.id = :orgId")
    Optional<String> findKeyById(@Param("orgId") Long orgId);
}
