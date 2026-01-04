// src/main/java/com/saymyname/persistence/repository/UserRepository.java
package com.saymyname.persistence.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saymyname.core.model.enums.SrsAlgorithm;
import com.saymyname.persistence.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

        Optional<UserEntity> findByPublicId(UUID publicId);

        @Query("select u.id from UserEntity u where u.publicId = :publicId")
        Optional<Long> findIdByPublicId(@Param("publicId") UUID publicId);

        /**
         * ✅ Charger user + emails (List) + identities (Set)
         * DISTINCT indispensable à cause du JOIN FETCH sur emails (sinon doublons).
         */
        @Query("""
                        select distinct u
                        from UserEntity u
                        left join fetch u.emails
                        left join fetch u.identities
                        where u.id = :id
                        """)
        Optional<UserEntity> findWithGraphById(@Param("id") Long id);

        @Query("""
                        select distinct u
                        from UserEntity u
                        left join fetch u.emails
                        left join fetch u.identities
                        where u.publicId = :publicId
                        """)
        Optional<UserEntity> findWithGraphByPublicId(@Param("publicId") UUID publicId);

        boolean existsByDisplayNameIgnoreCase(String displayName);

        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("update UserEntity u set u.displayName = :displayName where u.id = :id")
        int updateDisplayNameById(@Param("id") Long id, @Param("displayName") String displayName);

        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("update UserEntity u set u.srsAlgorithm = :algo where u.id = :id")
        int updateSrsAlgorithmById(@Param("id") Long id, @Param("algo") SrsAlgorithm algo);

        /**
         * ✅ Bump auth_version (atomic).
         * On laisse la DB mettre à jour auth_updated_at via ON UPDATE
         * CURRENT_TIMESTAMP.
         */
        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("""
                        update UserEntity u
                           set u.authVersion = u.authVersion + 1
                         where u.id = :id
                        """)
        int bumpAuthVersionById(@Param("id") Long id);

        interface UserSrsView {
                Long getId();

                SrsAlgorithm getSrsAlgorithm();
        }

        @Query("select u.id as id, u.srsAlgorithm as srsAlgorithm from UserEntity u where u.id = :id")
        Optional<UserSrsView> findSrsById(@Param("id") Long id);
}
