// src/main/java/com/saymyname/persistence/repository/UserEmailRepository.java
package com.saymyname.persistence.repository;

import java.time.LocalDateTime;
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
   * ✅ Email exists for a given user (case-insensitive).
   */
  @Query("""
      select ue
      from UserEmailEntity ue
      where ue.user.id = :userId
        and lower(ue.email) = lower(:email)
      """)
  Optional<UserEmailEntity> findByUserIdAndEmailIgnoreCase(
      @Param("userId") Long userId,
      @Param("email") String email);

  /**
   * ✅ Email éligible récupération: verified + (recoveryAllowed OR
   * recoveryEligibleAt <= now)
   */
  @Query("""
      select ue
      from UserEmailEntity ue
      where lower(ue.email) = lower(:email)
        and ue.verifiedAt is not null
        and (
          ue.recoveryAllowed = true
          or (ue.recoveryEligibleAt is not null and ue.recoveryEligibleAt <= :now)
        )
      """)
  Optional<UserEmailEntity> findRecoveryEligibleEmail(
      @Param("email") String email,
      @Param("now") LocalDateTime now);

  /**
   * ✅ Vérifie que l'utilisateur possède cet email ET qu'il est vérifié
   */
  @Query("""
      select (count(ue) > 0)
      from UserEmailEntity ue
      where ue.user.id = :userId
        and lower(ue.email) = lower(:email)
        and ue.verifiedAt is not null
      """)
  boolean existsVerifiedEmailForUserIgnoreCase(
      @Param("userId") Long userId,
      @Param("email") String email);

  /**
   * ✅ Renvoie le User associé à l'email (case-insensitive) AVEC emails +
   * identities.
   * IMPORTANT: DISTINCT pour éviter doublons liés aux JOIN FETCH.
   */
  @Query("""
      select distinct u
      from UserEmailEntity ue
      join ue.user u
      left join fetch u.emails
      left join fetch u.identities
      where lower(ue.email) = lower(:email)
      """)
  Optional<UserEntity> findUseremailsByEmailIgnoreCase(@Param("email") String email);

  /**
   * ✅ Renvoie le User associé à l'email de login (case-insensitive)
   * - loginAllowed = true
   * - verifiedAt != null
   * AVEC emails + identities.
   */
  @Query("""
      select distinct u
      from UserEmailEntity ue
      join ue.user u
      left join fetch u.emails
      left join fetch u.identities
      where lower(ue.email) = lower(:email)
        and ue.loginAllowed = true
        and ue.verifiedAt is not null
      """)
  Optional<UserEntity> findUseremailsByLoginEmailIgnoreCase(@Param("email") String email);

  // --------- Email primaire / flags ----------

  @Query("""
      select ue
      from UserEmailEntity ue
      where ue.user.id = :userId
        and ue.primary = true
      """)
  Optional<UserEmailEntity> findPrimaryByUserId(@Param("userId") Long userId);

  List<UserEmailEntity> findByUserId(Long userId);

  /**
   * ✅ Pour le front (sessionEmails) : emails autorisés login.
   * Tu peux décider d'inclure seulement verified (souvent préférable), au choix.
   */
  @Query("""
      select ue.email
      from UserEmailEntity ue
      where ue.user.id = :userId
        and ue.loginAllowed = true
        and ue.verifiedAt is not null
      """)
  List<String> listLoginAllowedEmails(@Param("userId") Long userId);

  // --------- Ops atomiques pour switcher le primaire ----------

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
      update UserEmailEntity ue
         set ue.primary = false
       where ue.user.id = :userId
         and ue.primary = true
      """)
  int clearPrimaryForUser(@Param("userId") Long userId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
      update UserEmailEntity ue
         set ue.primary = true
       where ue.id = :emailId
      """)
  int setPrimaryByEmailId(@Param("emailId") Long emailId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
      update UserEmailEntity ue
         set ue.verifiedAt = :now
       where ue.user.id = :userId
         and lower(ue.email) = lower(:email)
         and ue.verifiedAt is null
      """)
  int markVerifiedNowIfNull(
      @Param("userId") Long userId,
      @Param("email") String email,
      @Param("now") LocalDateTime now);

  // prévenir les doublons lors d’un changement d’email
  boolean existsByEmailIgnoreCaseAndUser_IdNot(String email, Long userId);
}
