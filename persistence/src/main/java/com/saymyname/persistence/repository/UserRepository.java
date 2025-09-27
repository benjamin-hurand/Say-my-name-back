// src/main/java/com/saymyname/persistence/repository/UserRepository.java
package com.saymyname.persistence.repository;

import com.saymyname.core.model.enums.SrsAlgorithm;
import com.saymyname.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByUsername(String username);

    Optional<UserEntity> findByEmail(String email);

    Boolean existsByEmail(String email);

    Boolean existsByUsername(String username);

    Optional<UserEntity> findByEmailIgnoreCase(String email);

    @Query("SELECT u FROM UserEntity u WHERE u.email = :value OR u.username = :value")
    Optional<UserEntity> findByEmailOrUsername(@Param("value") String value);

    // --- NOUVEAU: update ciblé, sans charger l'entité ---
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE UserEntity u SET u.srsAlgorithm = :algo WHERE u.id = :id")
    int updateSrsAlgorithmById(@Param("id") Long id, @Param("algo") SrsAlgorithm algo);

    // --- (Optionnel) projection minimaliste si tu veux relire juste après ---
    interface UserSrsView {
        Long getId();

        SrsAlgorithm getSrsAlgorithm();
    }

    @Query("SELECT u.id AS id, u.srsAlgorithm AS srsAlgorithm FROM UserEntity u WHERE u.id = :id")
    Optional<UserSrsView> findSrsById(@Param("id") Long id);
}
