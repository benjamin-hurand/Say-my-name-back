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

        /**
         * FOLLOWED + AND :
         * Compter les personnes suivies pour lesquelles TOUTES les attributes cibles du
         * game mode
         * existent (valeur non nulle / non vide).
         *
         * HAVING COUNT(DISTINCT gma.attribute_id) = COUNT(DISTINCT pa.attribute_id)
         * + garde-fou: COUNT(DISTINCT gma.attribute_id) > 0 (évite 0=0 si pas
         * d’attributs cibles)
         */
        @Query(value = """
                        SELECT COUNT(*) FROM (
                          SELECT s.person_id
                          FROM user_subscriptions s
                          JOIN game_modes_attributes gma
                            ON gma.game_mode_id = :gameModeId
                          LEFT JOIN persons_attributes pa
                            ON pa.person_id = s.person_id
                           AND pa.attribute_id = gma.attribute_id
                           AND pa.value IS NOT NULL AND pa.value <> ''
                          WHERE s.user_id = :userId
                          GROUP BY s.person_id
                          HAVING COUNT(DISTINCT gma.attribute_id) > 0
                             AND COUNT(DISTINCT gma.attribute_id) = COUNT(DISTINCT pa.attribute_id)
                        ) t
                        """, nativeQuery = true)
        long countFollowedEligibleAND(@Param("userId") Long userId, @Param("gameModeId") Long gameModeId);

        /**
         * FOLLOWED + OR :
         * Compter les personnes suivies pour lesquelles AU MOINS UN attribut cible du
         * game mode existe.
         * On passe par un DISTINCT sur s.person_id puisque plusieurs attributes peuvent
         * “matcher”.
         */
        @Query(value = """
                        SELECT COUNT(DISTINCT s.person_id)
                        FROM user_subscriptions s
                        JOIN game_modes_attributes gma
                          ON gma.game_mode_id = :gameModeId
                        JOIN persons_attributes pa
                          ON pa.person_id = s.person_id
                         AND pa.attribute_id = gma.attribute_id
                         AND pa.value IS NOT NULL AND pa.value <> ''
                        WHERE s.user_id = :userId
                        """, nativeQuery = true)
        long countFollowedEligibleOR(@Param("userId") Long userId, @Param("gameModeId") Long gameModeId);
}
