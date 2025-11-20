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

        Optional<UserEntity> findByUsername(String username);

        Boolean existsByUsername(String username);

        // --- NEW: accès par publicId (sans fetch join) ---
        Optional<UserEntity> findByPublicId(UUID publicId);

        @Query("select u.id from UserEntity u where u.publicId = :publicId")
        Optional<Long> findIdByPublicId(@Param("publicId") UUID publicId);

        // Charger un user + ses emails (fetch join) par id
        @Query("""
                        SELECT DISTINCT u
                        FROM UserEntity u
                        LEFT JOIN FETCH u.emails
                        WHERE u.id = :id
                        """)
        Optional<UserEntity> findWithEmailsById(@Param("id") Long id);

        // Charger un user + ses emails (fetch join) par username
        @Query("""
                        SELECT DISTINCT u
                        FROM UserEntity u
                        LEFT JOIN FETCH u.emails
                        WHERE u.username = :username
                        """)
        Optional<UserEntity> findWithEmailsByUsername(@Param("username") String username);

        // --- NEW: charger un user + ses emails (fetch join) par publicId ---
        @Query("""
                        SELECT DISTINCT u
                        FROM UserEntity u
                        LEFT JOIN FETCH u.emails
                        WHERE u.publicId = :publicId
                        """)
        Optional<UserEntity> findWithEmailsByPublicId(@Param("publicId") UUID publicId);

        // --- update ciblé SRS ---
        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("UPDATE UserEntity u SET u.srsAlgorithm = :algo WHERE u.id = :id")
        int updateSrsAlgorithmById(@Param("id") Long id, @Param("algo") SrsAlgorithm algo);

        // --- (Optionnel) projection minimaliste ---
        interface UserSrsView {
                Long getId();

                SrsAlgorithm getSrsAlgorithm();
        }

        @Query("SELECT u.id AS id, u.srsAlgorithm AS srsAlgorithm FROM UserEntity u WHERE u.id = :id")
        Optional<UserSrsView> findSrsById(@Param("id") Long id);
}
