package com.saymyname.persistence.repository;

import com.saymyname.persistence.entity.subscription.UserSubscriptionEntity;
import com.saymyname.persistence.entity.subscription.UserSubscriptionId;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.hibernate.jpa.HibernateHints.HINT_READ_ONLY;
import static org.hibernate.jpa.HibernateHints.HINT_FETCH_SIZE;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscriptionEntity, UserSubscriptionId> {

        boolean existsById(UserSubscriptionId id);

        long deleteByIdUserIdAndIdPersonId(Long userId, Long personId);

        long countByIdUserId(Long userId);

        /** Listing entités (read-only + fetch size indicatif). */
        @QueryHints({
                        @QueryHint(name = HINT_READ_ONLY, value = "true"),
                        @QueryHint(name = HINT_FETCH_SIZE, value = "500")
        })
        Page<UserSubscriptionEntity> findByIdUserId(Long userId, Pageable pageable);

        /** Projection IDs (read-only). */
        @Query("select e.id.personId from UserSubscriptionEntity e where e.id.userId = :userId")
        @QueryHints({
                        @QueryHint(name = HINT_READ_ONLY, value = "true")
        })
        Page<Long> findPersonIdsPageByUserId(@Param("userId") Long userId, Pageable pageable);

        /** Utilisé par bulkSubscribe pour détecter les existants en 1 requête. */
        @QueryHints({
                        @QueryHint(name = HINT_READ_ONLY, value = "true")
        })
        List<UserSubscriptionEntity> findByIdUserIdAndIdPersonIdIn(Long userId, List<Long> personIds);

        @Modifying
        @Query("delete from UserSubscriptionEntity e where e.id.userId = :userId and e.id.personId in :personIds")
        int deleteByUserIdAndPersonIdIn(@Param("userId") Long userId, @Param("personIds") List<Long> personIds);
}
