// src/main/java/com/saymyname/persistence/repository/PersonRepository.java
package com.saymyname.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saymyname.persistence.entity.organization.PersonEntity;

import jakarta.persistence.LockModeType;

@Repository
public interface PersonRepository extends JpaRepository<PersonEntity, Long>, PersonRepositoryCustom {

    @Query("""
            select p.id
            from PersonEntity p
            where p.tenantId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
            order by p.id
            """)
    List<Long> findAllIdsInCurrentTenant();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p
            from PersonEntity p
            where p.id = :personId
              and p.tenantId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
            """)
    Optional<PersonEntity> findByIdForUpdate(@Param("personId") Long personId);

    // JPQL → tenant auto via BaseTenantScoped filter
    // 1) p.facts + f.attribute (ManyToOne)
    @Query("""
            select distinct p
            from PersonEntity p
            left join fetch p.facts f
            left join fetch f.attribute attr
            where p.id = :personId
            """)
    Optional<PersonEntity> fetchFactsGraph(@Param("personId") Long personId);

    // JPQL → tenant auto
    // 2) p.photos
    @Query("""
            select distinct p
            from PersonEntity p
            left join fetch p.photos ph
            where p.id = :personId
            """)
    Optional<PersonEntity> fetchPhotos(@Param("personId") Long personId);
}
