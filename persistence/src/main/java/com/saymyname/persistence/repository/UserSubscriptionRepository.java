package com.saymyname.persistence.repository;

import com.saymyname.persistence.entity.organization.subscription.UserSubscriptionEntity;
import com.saymyname.persistence.entity.organization.subscription.UserSubscriptionKey;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static org.hibernate.jpa.HibernateHints.HINT_FETCH_SIZE;
import static org.hibernate.jpa.HibernateHints.HINT_READ_ONLY;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscriptionEntity, UserSubscriptionKey> {

        // ---------------------------------------------------------------------
        // Tenant-scoped derived queries
        // ---------------------------------------------------------------------

        boolean existsByTenantIdAndUserIdAndPersonId(Long tenantId, Long userId, Long personId);

        long deleteByTenantIdAndUserIdAndPersonId(Long tenantId, Long userId, Long personId);

        long countByTenantIdAndUserId(Long tenantId, Long userId);

        @QueryHints({
                        @QueryHint(name = HINT_READ_ONLY, value = "true"),
                        @QueryHint(name = HINT_FETCH_SIZE, value = "500")
        })
        Page<UserSubscriptionEntity> findByTenantIdAndUserId(Long tenantId, Long userId, Pageable pageable);

        @Query("""
                        select e.personId
                          from UserSubscriptionEntity e
                         where e.tenantId = :tenantId
                           and e.userId = :userId
                        """)
        @QueryHints(@QueryHint(name = HINT_READ_ONLY, value = "true"))
        Page<Long> findPersonIdsPageByTenantIdAndUserId(@Param("tenantId") Long tenantId,
                        @Param("userId") Long userId,
                        Pageable pageable);

        @QueryHints(@QueryHint(name = HINT_READ_ONLY, value = "true"))
        List<UserSubscriptionEntity> findByTenantIdAndUserIdAndPersonIdIn(Long tenantId, Long userId,
                        Collection<Long> personIds);

        @Query("""
                        select e.personId
                          from UserSubscriptionEntity e
                         where e.tenantId = :tenantId
                           and e.userId = :userId
                           and e.personId in :personIds
                        """)
        @QueryHints(@QueryHint(name = HINT_READ_ONLY, value = "true"))
        List<Long> findPersonIdsByTenantIdAndUserIdAndPersonIdIn(@Param("tenantId") Long tenantId,
                        @Param("userId") Long userId,
                        @Param("personIds") Collection<Long> personIds);

        @Modifying
        @Query("""
                        delete from UserSubscriptionEntity e
                         where e.tenantId = :tenantId
                           and e.userId = :userId
                           and e.personId in :personIds
                        """)
        int deleteByTenantIdAndUserIdAndPersonIdIn(@Param("tenantId") Long tenantId,
                        @Param("userId") Long userId,
                        @Param("personIds") Collection<Long> personIds);

}