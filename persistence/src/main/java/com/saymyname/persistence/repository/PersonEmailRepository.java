// src/main/java/com/saymyname/persistence/repository/PersonEmailRepository.java
package com.saymyname.persistence.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saymyname.persistence.entity.organization.people.PersonEmailEntity;

@Repository
public interface PersonEmailRepository extends JpaRepository<PersonEmailEntity, Long> {

  @Query("""
      select e
        from PersonEmailEntity e
       where e.tenantId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
         and e.person.id = :personId
       order by e.primary desc, e.createdAt desc
      """)
  List<PersonEmailEntity> findByPerson(@Param("personId") Long personId);

  @Query("""
      select e
        from PersonEmailEntity e
       where e.tenantId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
         and e.id = :id
      """)
  Optional<PersonEmailEntity> findByIdInCurrentOrg(@Param("id") Long id);

  @Query("""
      select e
        from PersonEmailEntity e
       where e.tenantId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
         and e.person.id = :personId
         and e.primary = true
      """)
  Optional<PersonEmailEntity> findPrimaryForPerson(@Param("personId") Long personId);

  @Query("""
      select case when count(e) > 0 then true else false end
        from PersonEmailEntity e
       where e.tenantId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
         and lower(e.email) = lower(:email)
         and e.active = true
      """)
  boolean existsActiveByEmailIgnoreCase(@Param("email") String email);

  @Query("""
      select e
        from PersonEmailEntity e
       where e.tenantId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
         and lower(e.email) = lower(:email)
         and e.active = true
      """)
  Optional<PersonEmailEntity> findFirstActiveByEmailIgnoreCase(@Param("email") String email);

  @Modifying
  @Query("""
      update PersonEmailEntity e
         set e.primary = false
       where e.tenantId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
         and e.person.id = :personId
         and e.id <> :keepId
      """)
  int clearPrimaryExcept(@Param("personId") Long personId, @Param("keepId") Long keepId);

  @Modifying
  @Query("""
      update PersonEmailEntity e
         set e.primary = false
       where e.tenantId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
         and e.person.id = :personId
      """)
  int clearPrimary(@Param("personId") Long personId);

  @Modifying
  @Query("""
      delete from PersonEmailEntity e
       where e.tenantId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
         and e.id = :id
      """)
  void deleteByIdInCurrentOrg(@Param("id") Long id);

  // -------- NEW: agrégation batch du statut e-mail --------
  // 0 = NONE, 1 = HAS, 2 = PRIMARY, 3 = PRIMARY_VERIFIED
  interface PersonEmailStatusRow {
    Long getPersonId();

    Integer getStatus();
  }

  @Query(value = """
      SELECT pe.person_id AS personId,
             CASE
               WHEN MAX(CASE WHEN pe.active=1 AND pe.primary_flag=1 AND pe.verified_at IS NOT NULL THEN 1 ELSE 0 END)=1 THEN 3
               WHEN MAX(CASE WHEN pe.active=1 AND pe.primary_flag=1 THEN 1 ELSE 0 END)=1 THEN 2
               WHEN MAX(CASE WHEN pe.active=1 THEN 1 ELSE 0 END)=1 THEN 1
               ELSE 0
             END AS status
        FROM person_emails pe
       WHERE pe.tenant_id = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
         AND pe.person_id IN (:personIds)
       GROUP BY pe.person_id
      """, nativeQuery = true)
  List<PersonEmailStatusRow> fetchEmailStatusBatch(@Param("personIds") List<Long> personIds);
}
