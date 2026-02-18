// src/main/java/com/saymyname/persistence/repository/ChangeRequestItemRepository.java
package com.saymyname.persistence.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saymyname.core.model.enums.ChangeRequestItemStatus;
import com.saymyname.persistence.entity.organization.ChangeRequestItemEntity;

@Repository
public interface ChangeRequestItemRepository extends JpaRepository<ChangeRequestItemEntity, Long> {

    /*
     * ===================
     * 1) CANCEL — seulement PENDING
     * ===================
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
             UPDATE ChangeRequestItemEntity i
                SET i.resolutionStatus = :canceled,
                    i.resolutionComment = CONCAT(
                        COALESCE(i.resolutionComment, ''),
                        CASE WHEN i.resolutionComment IS NULL OR i.resolutionComment = '' THEN '' ELSE ' | ' END,
                        :comment
                    )
              WHERE i.changeRequest.id = :crId
                AND i.organizationId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
                AND i.resolutionStatus = :pending
            """)
    int cancelAllPendingByCrId(@Param("crId") Long changeRequestId,
            @Param("canceled") ChangeRequestItemStatus canceled,
            @Param("pending") ChangeRequestItemStatus pending,
            @Param("comment") String comment);

    /*
     * =================================================
     * 2) RESOLUTION MIXTE (APPROVED / REJECTED) en un seul UPDATE
     * - Met uniquement les items PENDING listés (par sécurité).
     * - 'allIds' = union(approvedIds ∪ rejectedIds) pour éviter IN () vide.
     * =================================================
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
             UPDATE ChangeRequestItemEntity i
                SET i.resolutionStatus =
                      CASE
                        WHEN i.id IN :approvedIds THEN :approved
                        WHEN i.id IN :rejectedIds THEN :rejected
                        ELSE i.resolutionStatus
                      END,
                    i.resolutionComment = CONCAT(
                        COALESCE(i.resolutionComment, ''),
                        CASE WHEN i.resolutionComment IS NULL OR i.resolutionComment = '' THEN '' ELSE ' | ' END,
                        :comment
                    )
              WHERE i.changeRequest.id = :crId
                AND i.organizationId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
                AND i.id IN :allIds
                AND i.resolutionStatus = :pending
            """)
    int resolveMixedByIdsPendingOnly(@Param("crId") Long changeRequestId,
            @Param("allIds") Collection<Long> allIds,
            @Param("approvedIds") Collection<Long> approvedIds,
            @Param("rejectedIds") Collection<Long> rejectedIds,
            @Param("approved") ChangeRequestItemStatus approved,
            @Param("rejected") ChangeRequestItemStatus rejected,
            @Param("pending") ChangeRequestItemStatus pending,
            @Param("comment") String comment);

    /*
     * =========== Comptages pour recalcul de statut d'enveloppe
     * ===========
     */
    @Query("""
                SELECT COUNT(i) FROM ChangeRequestItemEntity i
                 WHERE i.changeRequest.id = :crId
                   AND i.organizationId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
            """)
    long countAllByCr(@Param("crId") Long changeRequestId);

    @Query("""
                SELECT COUNT(i) FROM ChangeRequestItemEntity i
                 WHERE i.changeRequest.id = :crId
                   AND i.organizationId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
                   AND i.resolutionStatus = :status
            """)
    long countByCrAndStatus(@Param("crId") Long changeRequestId,
            @Param("status") ChangeRequestItemStatus status);

    /*
     * == Chargements ciblés pour construire les listes à appliquer ==
     */
    @Query("""
                SELECT i FROM ChangeRequestItemEntity i
                 JOIN FETCH i.changeRequest cr
                 LEFT JOIN FETCH i.fact f
                 WHERE cr.id = :crId
                   AND i.organizationId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
                   AND i.id IN :ids
            """)
    List<ChangeRequestItemEntity> findAllByCrAndIds(@Param("crId") Long changeRequestId,
            @Param("ids") Collection<Long> ids);

    /**
     * Met à NULL fact_id pour les items d'enveloppes RESOLUES
     * pointant vers des facts supprimés et expirés (is_deleted=1 AND valid_to < :cutoff).
     * Multi-tenant: organisation courante seulement.
     *
     * NOTE: native SQL pour gérer proprement la FK (fact_id).
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE change_request_items cri
            JOIN change_requests cr
              ON cr.id = cri.change_request_id
             AND cr.organization_id = cri.organization_id
            JOIN facts f
              ON f.id = cri.fact_id
             AND f.tenant_id = cri.tenant_id
            SET cri.fact_id = NULL
            WHERE cri.organization_id = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
              AND cr.status <> 'PENDING'
              AND cri.fact_id IS NOT NULL
              AND f.is_deleted = 1
              AND f.valid_to IS NOT NULL
              AND f.valid_to < :cutoffExclusive
            """, nativeQuery = true)
    int nullifyFactFkForResolvedAndExpiredTombstones(
            @Param("cutoffExclusive") LocalDateTime cutoffExclusive);

    @Query("""
            SELECT i.id
              FROM ChangeRequestItemEntity i
             WHERE i.changeRequest.id = :crId
               AND i.organizationId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
               AND i.resolutionStatus = :pending
            """)
    List<Long> findPendingIdsByCr(@Param("crId") Long changeRequestId,
            @Param("pending") ChangeRequestItemStatus pending);
}
