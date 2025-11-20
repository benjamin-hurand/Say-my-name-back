package com.saymyname.persistence.repository;

import java.util.List;
import java.util.Optional;

import com.saymyname.persistence.entity.UserEmailEntity;
import com.saymyname.persistence.entity.UserEntity;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserEmailRepository extends JpaRepository<UserEmailEntity, Long> {

    // --------- Lookups (case-insensitive) ----------
    boolean existsByEmailIgnoreCase(String email);

    Optional<UserEmailEntity> findByEmailIgnoreCase(String email);

    /**
     * Renvoie le User associé à l'email (case-insensitive), AVEC ses emails
     * chargés.
     * Utile pour construire un modèle complet sans LazyInitializationException.
     */
    @Query("""
            select u
            from UserEmailEntity ue
            join ue.user u
            left join fetch u.emails
            where lower(ue.email) = lower(:email)
            """)
    Optional<UserEntity> findUserWithEmailsByEmailIgnoreCase(@Param("email") String email);

    // --------- Email primaire / flags ----------
    @Query("select ue from UserEmailEntity ue where ue.user.id = :userId and ue.primary = true")
    Optional<UserEmailEntity> findPrimaryByUserId(@Param("userId") Long userId);

    List<UserEmailEntity> findByUserId(Long userId);

    @Query("select ue.email from UserEmailEntity ue where ue.user.id = :userId and ue.loginAllowed = true")
    List<String> listLoginAllowedEmails(@Param("userId") Long userId);

    // --------- Ops atomiques pour switcher le primaire ----------
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update UserEmailEntity ue set ue.primary = false where ue.user.id = :userId and ue.primary = true")
    int clearPrimaryForUser(@Param("userId") Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update UserEmailEntity ue set ue.primary = true where ue.id = :emailId")
    int setPrimaryByEmailId(@Param("emailId") Long emailId);

    // (Optionnel) prévenir les doublons lors d’un changement d’email
    boolean existsByEmailIgnoreCaseAndUser_IdNot(String email, Long userId);
}
