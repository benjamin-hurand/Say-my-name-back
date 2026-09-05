// src/main/java/com/saymyname/persistence/repository/ChangeRequestRepository.java
package com.saymyname.persistence.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saymyname.core.model.enums.ChangeRequestStatus;
import com.saymyname.persistence.entity.organization.ChangeRequestEntity;

@Repository
public interface ChangeRequestRepository
    extends JpaRepository<ChangeRequestEntity, Long>, ChangeRequestRepositoryCustom {

  /* =================== Loads (org-scoped) =================== */

  @Query("""
          select cr
            from ChangeRequestEntity cr
            join fetch cr.person p
            join fetch cr.requester r
            join fetch cr.attribute a
       left join fetch cr.items i
       left join fetch i.fact pa
           where cr.id = :id
             and cr.tenantId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
      """)
  Optional<ChangeRequestEntity> findByIdDeepOrgScoped(@Param("id") Long id);

  @Query("""
          select cr
            from ChangeRequestEntity cr
           where cr.id = :id
             and cr.tenantId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
      """)
  Optional<ChangeRequestEntity> findByIdOrgScoped(@Param("id") Long id);

  /* =================== Open by triplet (org-scoped) =================== */

  @Query("""
          select cr
            from ChangeRequestEntity cr
            join cr.person p
            join cr.requester r
            join cr.attribute a
           where p.id = :personId
             and r.id = :requesterId
             and a.id = :attributeId
             and cr.status in :openStatuses
             and cr.tenantId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
        order by cr.createdAt desc
      """)
  Optional<ChangeRequestEntity> findFirstOpenByTripletOrgScoped(
      @Param("personId") Long personId,
      @Param("requesterId") Long requesterId,
      @Param("attributeId") Long attributeId,
      @Param("openStatuses") Collection<ChangeRequestStatus> openStatuses);

  /* =================== Listings (org-scoped) =================== */

  @Query("""
          select distinct cr
            from ChangeRequestEntity cr
            join fetch cr.requester r
            join fetch cr.person p
            join fetch cr.attribute a
       left join fetch cr.resolvedBy rb
       left join fetch cr.items i
       left join fetch i.fact pa
           where r.id = :userId
             and cr.status in :statuses
             and cr.tenantId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
        order by cr.createdAt desc
      """)
  List<ChangeRequestEntity> findByUserIdAndStatusesDeepOrgScoped(
      @Param("userId") Long userId,
      @Param("statuses") Collection<ChangeRequestStatus> statuses);

  @Query("""
          select distinct cr
            from ChangeRequestEntity cr
            join fetch cr.requester r
            join fetch cr.person p
            join fetch cr.attribute a
       left join fetch cr.resolvedBy rb
       left join fetch cr.items i
       left join fetch i.fact pa
           where p.id = :personId
             and cr.status in :statuses
             and cr.tenantId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
        order by cr.createdAt desc
      """)
  List<ChangeRequestEntity> findByPersonIdAndStatusesDeepOrgScoped(
      @Param("personId") Long personId,
      @Param("statuses") Collection<ChangeRequestStatus> statuses);

  /** Impact de suppression : nb de change requests PENDING par attribut. */
  @Query(value = """
          SELECT cr.attribute_id AS attributeId,
                 COUNT(*) AS pendingCount
            FROM change_requests cr
           WHERE cr.attribute_id IN (:attributeIds)
             AND cr.tenant_id = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
             AND cr.status = 'PENDING'
           GROUP BY cr.attribute_id
      """, nativeQuery = true)
  List<Object[]> countPendingByAttributeIds(@Param("attributeIds") Collection<Long> attributeIds);

  /* =================== Bulk update header (org-scoped) =================== */

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
          update ChangeRequestEntity cr
             set cr.status = :status,
                 cr.resolvedAt = :resolvedAt,
                 cr.resolvedBy.id = :resolvedById,
                 cr.resolutionComment = :comment,
                 cr.updatedAt = :resolvedAt
           where cr.id = :crId
             and cr.tenantId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
      """)
  int updateResolutionMetaOrgScoped(@Param("crId") Long crId,
      @Param("status") ChangeRequestStatus status,
      @Param("resolvedAt") LocalDateTime resolvedAt,
      @Param("resolvedById") Long resolvedById,
      @Param("comment") String resolutionComment);
}
