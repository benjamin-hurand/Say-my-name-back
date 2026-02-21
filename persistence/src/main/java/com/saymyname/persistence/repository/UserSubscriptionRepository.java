package com.saymyname.persistence.repository;

import com.saymyname.persistence.entity.organization.subscription.UserSubscriptionEntity;
import com.saymyname.persistence.entity.organization.subscription.UserSubscriptionId;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.hibernate.jpa.HibernateHints.HINT_FETCH_SIZE;
import static org.hibernate.jpa.HibernateHints.HINT_READ_ONLY;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscriptionEntity, UserSubscriptionId> {

    boolean existsById(@NonNull UserSubscriptionId id);

    // -------------------------
    // Tenant-scoped derived queries
    // -------------------------

    long deleteByIdTenantIdAndIdUserIdAndIdPersonId(Long tenantId, Long userId, Long personId);

    long countByIdTenantIdAndIdUserId(Long tenantId, Long userId);

    /** Listing entités (read-only + fetch size indicatif). */
    @QueryHints({
            @QueryHint(name = HINT_READ_ONLY, value = "true"),
            @QueryHint(name = HINT_FETCH_SIZE, value = "500")
    })
    Page<UserSubscriptionEntity> findByIdTenantIdAndIdUserId(Long tenantId, Long userId, Pageable pageable);

    /** Projection IDs (read-only). */
    @Query("""
                select e.id.personId
                  from UserSubscriptionEntity e
                 where e.id.tenantId = :tenantId
                   and e.id.userId = :userId
            """)
    @QueryHints({
            @QueryHint(name = HINT_READ_ONLY, value = "true")
    })
    Page<Long> findPersonIdsPageByTenantIdAndUserId(@Param("tenantId") Long tenantId,
            @Param("userId") Long userId,
            Pageable pageable);

    /** Utilisé par bulkSubscribe pour détecter les existants en 1 requête. */
    @QueryHints({
            @QueryHint(name = HINT_READ_ONLY, value = "true")
    })
    List<UserSubscriptionEntity> findByIdTenantIdAndIdUserIdAndIdPersonIdIn(Long tenantId, Long userId,
            List<Long> personIds);

    @Modifying
    @Query("""
                delete from UserSubscriptionEntity e
                 where e.id.tenantId = :tenantId
                   and e.id.userId   = :userId
                   and e.id.personId in :personIds
            """)
    int deleteByTenantIdAndUserIdAndPersonIdIn(@Param("tenantId") Long tenantId,
            @Param("userId") Long userId,
            @Param("personIds") List<Long> personIds);

    // -------------------------
    // Legacy methods removed / replaced
    // -------------------------
    // NOTE:
    // Les méthodes ORG-legacy suivantes ne sont plus valides depuis que tenant_id
    // est dans la PK.
    // - deleteByIdUserIdAndIdPersonId
    // - countByIdUserId
    // - findByIdUserId
    // - findPersonIdsPageByUserId
    // - findByIdUserIdAndIdPersonIdIn
    // - deleteByUserIdAndPersonIdIn
    //
    // => elles doivent être supprimées (ou laissées commentées) pour éviter les
    // usages ambigus.

    // -------------------------
    // GameMode / Facts eligibility: keep API but neutralize safely
    // -------------------------
    // Ces deux méthodes reposaient sur tables supprimées (game_modes_attributes,
    // person_attributes)
    // et OrgContext. Tant que la refacto "gamemode -> attributes/facts" n'est pas
    // finie,
    // le plus safe est de :
    // - soit les supprimer + corriger les call-sites,
    // - soit retourner 0 en stub temporaire pour ne pas bloquer la migration
    // tenant.
    //
    // Ci-dessous: stub TEMPORAIRE (à remplacer quand la nouvelle logique Facts est
    // en place).

    @Query(value = "select 0", nativeQuery = true)
    long countFollowedEligibleAND(@Param("tenantId") Long tenantId,
            @Param("userId") Long userId,
            @Param("gameModeId") Long gameModeId);

    @Query(value = "select 0", nativeQuery = true)
    long countFollowedEligibleOR(@Param("tenantId") Long tenantId,
            @Param("userId") Long userId,
            @Param("gameModeId") Long gameModeId);
}