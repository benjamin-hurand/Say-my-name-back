package com.saymyname.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saymyname.core.model.enums.AttemptStatus;
import com.saymyname.persistence.entity.ChallengeAttemptEntity;
import com.saymyname.persistence.entity.UserEntity;

@Repository
public interface ChallengeAttemptRepository extends JpaRepository<ChallengeAttemptEntity, Long> {

  @Query("""
       SELECT DISTINCT a
       FROM ChallengeAttemptEntity a
         JOIN FETCH a.challengeVersion v
         JOIN FETCH v.challenge c
         JOIN FETCH c.gameMode
         JOIN FETCH v.questions q
         JOIN FETCH q.person p
       WHERE a.id = :id
       ORDER BY function('RAND')
      """)
  Optional<ChallengeAttemptEntity> findByIdWithAll(@Param("id") Long id);

  /**
   * Vérifie qu’une tentative existe pour cet userId et attemptId,
   * n’a pas encore démarré (attemptStart IS NULL)
   * et n’est pas terminée (attemptEnd IS NULL).
   */
  boolean existsByIdAndUserIdAndAttemptStartIsNullAndAttemptEndIsNull(
      Long attemptId,
      Long userId);

  /**
   * Supprime toutes les tentatives pour l'utilisateur userId
   * dont attemptStart ET attemptEnd sont tous deux NULL.
   */
  @Modifying
  @Query("""
        DELETE FROM ChallengeAttemptEntity a
        WHERE a.user.id = :userId
          AND (a.attemptStart IS NULL
          OR a.attemptEnd   IS NULL)
      """)
  void deleteIncompleteAttemptsByUserId(@Param("userId") Long userId);

  @Modifying
  @Query("""
        UPDATE ChallengeAttemptEntity a
        SET a.status = 'ABANDONED'
        WHERE a.id = :id
      """)
  void markAbandoned(@Param("id") Long id);

  List<ChallengeAttemptEntity> findByStatusAndAttemptStartBefore(AttemptStatus inProgress, LocalDateTime cutoff);

}
