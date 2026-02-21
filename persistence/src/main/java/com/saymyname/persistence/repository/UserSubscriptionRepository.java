package com.saymyname.persistence.repository;

import com.saymyname.persistence.entity.organization.subscription.UserSubscriptionEntity;
import com.saymyname.persistence.entity.organization.subscription.UserSubscriptionKey;

import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

import static org.hibernate.jpa.HibernateHints.HINT_FETCH_SIZE;
import static org.hibernate.jpa.HibernateHints.HINT_READ_ONLY;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscriptionEntity, UserSubscriptionKey> {

        // --- Exists / Delete / Count (tenant scope assuré par Hibernate Filter)
        boolean existsByUserIdAndPersonId(Long userId, Long personId);

        long deleteByUserIdAndPersonId(Long userId, Long personId);

        long countByUserId(Long userId);

        @QueryHints({
                        @QueryHint(name = HINT_READ_ONLY, value = "true"),
                        @QueryHint(name = HINT_FETCH_SIZE, value = "500")
        })
        Page<UserSubscriptionEntity> findByUserId(Long userId, Pageable pageable);

        @Query("""
                        select e.personId
                          from UserSubscriptionEntity e
                         where e.userId = :userId
                        """)
        @QueryHints(@QueryHint(name = HINT_READ_ONLY, value = "true"))
        Page<Long> findPersonIdsPageByUserId(@Param("userId") Long userId, Pageable pageable);

        @QueryHints(@QueryHint(name = HINT_READ_ONLY, value = "true"))
        List<UserSubscriptionEntity> findByUserIdAndPersonIdIn(Long userId, Collection<Long> personIds);

        @Modifying
        @Query("""
                        delete from UserSubscriptionEntity e
                         where e.userId = :userId
                           and e.personId in :personIds
                        """)
        int deleteByUserIdAndPersonIdIn(@Param("userId") Long userId,
                        @Param("personIds") Collection<Long> personIds);

        // --- Stubs temporaires (tu peux les garder)
        @Query(value = "select 0", nativeQuery = true)
        long countFollowedEligibleAND(@Param("userId") Long userId, @Param("gameModeId") Long gameModeId);

        @Query(value = "select 0", nativeQuery = true)
        long countFollowedEligibleOR(@Param("userId") Long userId, @Param("gameModeId") Long gameModeId);
}