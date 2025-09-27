package com.saymyname.persistence.repository.course;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.persistence.entity.course.KnowledgeEntity;

@Repository
public interface KnowledgeRepository extends JpaRepository<KnowledgeEntity, Long> {

  // --- UPSERT (inchangé) -----------------------------------------

  @Modifying
  @Transactional
  @Query(value = """
        INSERT INTO knowledges
          (user_id, game_mode_id, person_id,
           next_review_date, total_repetition_count,
           srs_streak, global_streak, ease_factor,
           status, last_review_date,
           success_count, failure_count, stability, difficulty)
        VALUES
          (:userId, :gameModeId, :personId,
           :nextReviewDate, :totalCount,
           :srs_streak, :global_streak, :easeFactor,
           :status, :lastReviewDate,
           :successCount, :failureCount, :stability, :difficulty)
        ON DUPLICATE KEY UPDATE
          next_review_date           = VALUES(next_review_date),
          total_repetition_count     = VALUES(total_repetition_count),
          srs_streak                 = VALUES(srs_streak),
          global_streak              = VALUES(global_streak),
          ease_factor                = VALUES(ease_factor),
          status                     = VALUES(status),
          last_review_date           = VALUES(last_review_date),
          success_count              = VALUES(success_count),
          failure_count              = VALUES(failure_count),
          stability                  = VALUES(stability),
          difficulty                 = VALUES(difficulty)
      """, nativeQuery = true)
  void upsertKnowledge(
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("personId") Long personId,
      @Param("nextReviewDate") LocalDateTime nextReviewDate,
      @Param("totalCount") int totalCount,
      @Param("srs_streak") int srs_streak,
      @Param("global_streak") int global_streak,
      @Param("easeFactor") BigDecimal easeFactor,
      @Param("status") String status,
      @Param("lastReviewDate") LocalDateTime lastReviewDate,
      @Param("successCount") int successCount,
      @Param("failureCount") int failureCount,
      @Param("stability") double stability,
      @Param("difficulty") double difficulty);

  // --- INSERT BATCH PAR SCOPE ------------------------------------

  /**
   * FOLLOWED : insère des UNKNOWN aléatoires pour les personnes suivies non
   * encore présentes
   */
  @Modifying
  @Transactional
  @Query(value = """
      INSERT IGNORE INTO knowledges (
        user_id, game_mode_id, person_id, status,
        next_review_date, last_review_date, total_repetition_count,
        failure_count, success_count, srs_streak, global_streak,
        ease_factor, difficulty, stability
      )
      SELECT
        :userId        AS user_id,
        :gameModeId    AS game_mode_id,
        s.person_id    AS person_id,
        'UNKNOWN'      AS status,
        CURRENT_TIMESTAMP AS next_review_date,
        NULL           AS last_review_date,
        0              AS total_repetition_count,
        0              AS failure_count,
        0              AS success_count,
        0              AS srs_streak,
        0              AS global_streak,
        :initialEf     AS ease_factor,
        :initialDiff   AS difficulty,
        :initialStab   AS stability
      FROM user_subscriptions s
      WHERE s.user_id = :userId
        AND NOT EXISTS (
          SELECT 1
          FROM knowledges k
          WHERE k.user_id = :userId
            AND k.game_mode_id = :gameModeId
            AND k.person_id = s.person_id
        )
      ORDER BY RAND()
      LIMIT :limit
      """, nativeQuery = true)
  int insertNextKnowledgesForCourseFollowed(
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("initialEf") double initialEaseFactor,
      @Param("initialDiff") double initialDifficuly,
      @Param("initialStab") double initialStability,
      @Param("limit") int limit);

  /**
   * ALL : insère des UNKNOWN aléatoires parmi toutes les personnes non encore
   * présentes
   */
  @Modifying
  @Transactional
  @Query(value = """
      INSERT IGNORE INTO knowledges (
        user_id, game_mode_id, person_id, status,
        next_review_date, last_review_date, total_repetition_count,
        failure_count, success_count, srs_streak, global_streak,
        ease_factor, difficulty, stability
      )
      SELECT
        :userId        AS user_id,
        :gameModeId    AS game_mode_id,
        p.id           AS person_id,
        'UNKNOWN'      AS status,
        CURRENT_TIMESTAMP AS next_review_date,
        NULL           AS last_review_date,
        0              AS total_repetition_count,
        0              AS failure_count,
        0              AS success_count,
        0              AS srs_streak,
        0              AS global_streak,
        :initialEf     AS ease_factor,
        :initialDiff   AS difficulty,
        :initialStab   AS stability
      FROM persons p
      WHERE NOT EXISTS (
        SELECT 1
        FROM knowledges k
        WHERE k.user_id = :userId
          AND k.game_mode_id = :gameModeId
          AND k.person_id = p.id
      )
      ORDER BY RAND()
      LIMIT :limit
      """, nativeQuery = true)
  int insertNextKnowledgesForCourseAll(
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("initialEf") double initialEaseFactor,
      @Param("initialDiff") double initialDifficuly,
      @Param("initialStab") double initialStability,
      @Param("limit") int limit);

  // --- COMPTAGE ----------------------------------------------------

  int countByUserIdAndGameModeIdAndStatusIn(
      Long userId,
      Long gameModeId,
      Collection<KnowledgeStatus> statuses);

  // --- FALLBACK / SINGLE ------------------------------------------

  Optional<KnowledgeEntity> findByUserIdAndGameModeIdAndPersonId(
      Long userId, Long gameModeId, Long personId);

  // --- POOLS FOLLOWED (LIMIT 1) -----------------------------------

  /** UNKNOWN — FOLLOWED */
  @Query(value = """
      SELECT k.*
        FROM knowledges k
       WHERE k.user_id       = :userId
         AND k.game_mode_id  = :gameModeId
         AND k.status        = 'UNKNOWN'
         AND k.person_id IN (
           SELECT s.person_id FROM user_subscriptions s WHERE s.user_id = :userId
         )
         AND ( :allowRepeat = true OR k.person_id <> :lastPersonId )
       ORDER BY k.id ASC
       LIMIT 1
      """, nativeQuery = true)
  KnowledgeEntity findFirstNewItemFollowed(
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat);

  /** DISCOVERED — FOLLOWED */
  @Query(value = """
      SELECT k.*
        FROM knowledges k
       WHERE k.user_id       = :userId
         AND k.game_mode_id  = :gameModeId
         AND k.status        = 'DISCOVERED'
         AND k.person_id IN (
           SELECT s.person_id FROM user_subscriptions s WHERE s.user_id = :userId
         )
         AND ( :allowRepeat = true OR k.person_id <> :lastPersonId )
       ORDER BY k.id ASC
       LIMIT 1
      """, nativeQuery = true)
  KnowledgeEntity findFirstNotSoNewItemFollowed(
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat);

  /** LEARNED – erreurs récentes — FOLLOWED */
  @Query(value = """
      SELECT k.*
        FROM knowledges k
       WHERE k.user_id          = :userId
         AND k.game_mode_id     = :gameModeId
         AND k.status           = 'LEARNED'
         AND k.global_streak   <= 0
         AND k.last_review_date >= CURRENT_TIMESTAMP - INTERVAL 1 DAY
         AND k.person_id IN (
           SELECT s.person_id FROM user_subscriptions s WHERE s.user_id = :userId
         )
         AND ( :allowRepeat = true OR k.person_id <> :lastPersonId )
       ORDER BY k.last_review_date ASC
       LIMIT 1
      """, nativeQuery = true)
  KnowledgeEntity findFirstRecentErrorFollowed(
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat);

  /** LEARNED – SRS dues — FOLLOWED */
  @Query(value = """
      SELECT k.*
        FROM knowledges k
       WHERE k.user_id           = :userId
         AND k.game_mode_id      = :gameModeId
         AND k.status            = 'LEARNED'
         AND k.next_review_date <= CURRENT_TIMESTAMP
         AND k.person_id IN (
           SELECT s.person_id FROM user_subscriptions s WHERE s.user_id = :userId
         )
         AND ( :allowRepeat = true OR k.person_id <> :lastPersonId )
       ORDER BY k.next_review_date ASC
       LIMIT 1
      """, nativeQuery = true)
  KnowledgeEntity findFirstSrsDueFollowed(
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat);

  /** MASTERED / LEARNED futures dues — random — FOLLOWED */
  @Query(value = """
      SELECT k.*
        FROM knowledges k
       WHERE k.user_id       = :userId
         AND k.game_mode_id  = :gameModeId
         AND (
              k.status = 'MASTERED'
           OR (
                  k.status = 'LEARNED'
              AND k.next_review_date > CURRENT_TIMESTAMP
              AND NOT (
                  k.global_streak <= 0
                  AND k.last_review_date >= CURRENT_TIMESTAMP - INTERVAL 1 DAY
              )
           )
         )
         AND k.person_id IN (
           SELECT s.person_id FROM user_subscriptions s WHERE s.user_id = :userId
         )
         AND ( :allowRepeat = true OR k.person_id <> :lastPersonId )
       ORDER BY RAND()
       LIMIT 1
      """, nativeQuery = true)
  KnowledgeEntity findRevisionFollowed(
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat);

  // --- POOLS ALL (LIMIT 1) ----------------------------------------

  /** UNKNOWN — ALL */
  @Query(value = """
      SELECT k.*
        FROM knowledges k
       WHERE k.user_id       = :userId
         AND k.game_mode_id  = :gameModeId
         AND k.status        = 'UNKNOWN'
         AND ( :allowRepeat = true OR k.person_id <> :lastPersonId )
       ORDER BY k.id ASC
       LIMIT 1
      """, nativeQuery = true)
  KnowledgeEntity findFirstNewItemAll(
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat);

  /** DISCOVERED — ALL */
  @Query(value = """
      SELECT k.*
        FROM knowledges k
       WHERE k.user_id       = :userId
         AND k.game_mode_id  = :gameModeId
         AND k.status        = 'DISCOVERED'
         AND ( :allowRepeat = true OR k.person_id <> :lastPersonId )
       ORDER BY k.id ASC
       LIMIT 1
      """, nativeQuery = true)
  KnowledgeEntity findFirstNotSoNewItemAll(
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat);

  /** LEARNED – erreurs récentes — ALL */
  @Query(value = """
      SELECT k.*
        FROM knowledges k
       WHERE k.user_id          = :userId
         AND k.game_mode_id     = :gameModeId
         AND k.status           = 'LEARNED'
         AND k.global_streak   <= 0
         AND k.last_review_date >= CURRENT_TIMESTAMP - INTERVAL 1 DAY
         AND ( :allowRepeat = true OR k.person_id <> :lastPersonId )
       ORDER BY k.last_review_date ASC
       LIMIT 1
      """, nativeQuery = true)
  KnowledgeEntity findFirstRecentErrorAll(
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat);

  /** LEARNED – SRS dues — ALL */
  @Query(value = """
      SELECT k.*
        FROM knowledges k
       WHERE k.user_id           = :userId
         AND k.game_mode_id      = :gameModeId
         AND k.status            = 'LEARNED'
         AND k.next_review_date <= CURRENT_TIMESTAMP
         AND ( :allowRepeat = true OR k.person_id <> :lastPersonId )
       ORDER BY k.next_review_date ASC
       LIMIT 1
      """, nativeQuery = true)
  KnowledgeEntity findFirstSrsDueAll(
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat);

  /** MASTERED / LEARNED futures dues — random — ALL */
  @Query(value = """
      SELECT k.*
        FROM knowledges k
       WHERE k.user_id       = :userId
         AND k.game_mode_id  = :gameModeId
         AND (
              k.status = 'MASTERED'
           OR (
                  k.status = 'LEARNED'
              AND k.next_review_date > CURRENT_TIMESTAMP
              AND NOT (
                  k.global_streak <= 0
                  AND k.last_review_date >= CURRENT_TIMESTAMP - INTERVAL 1 DAY
              )
           )
         )
         AND ( :allowRepeat = true OR k.person_id <> :lastPersonId )
       ORDER BY RAND()
       LIMIT 1
      """, nativeQuery = true)
  KnowledgeEntity findRevisionAll(
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat);

  // --- LISTE -------------------------------------------------------

  List<KnowledgeEntity> findByGameModeIdAndUserIdAndStatusNot(
      Long gameModeId,
      Long userId,
      KnowledgeStatus statusExcluded);

  /** Nombre de personnes suivies (candidates FOLLOWED) */
  @Query(value = "SELECT COUNT(*) FROM user_subscriptions WHERE user_id = :userId", nativeQuery = true)
  int countFollowedCandidates(@Param("userId") Long userId);

  /** Nombre total de persons (candidates ALL) */
  @Query(value = "SELECT COUNT(*) FROM persons", nativeQuery = true)
  int countAllPersonsTotal();

  /** Total de knowledges pour (user, gameMode) */
  @Query(value = """
      SELECT COUNT(*) FROM knowledges
      WHERE user_id = :userId AND game_mode_id = :gameModeId
      """, nativeQuery = true)
  int countAllKnowledges(@Param("userId") Long userId, @Param("gameModeId") Long gameModeId);

  // --- SRS DUE & RECENT ERRORS (FOLLOWED scope) -----------------

  @Query(value = """
      SELECT COUNT(*) FROM knowledges k
      WHERE k.user_id = :userId
        AND k.game_mode_id = :gameModeId
        AND k.status = 'LEARNED'
        AND k.next_review_date <= CURRENT_TIMESTAMP
        AND k.person_id IN (SELECT s.person_id FROM user_subscriptions s WHERE s.user_id = :userId)
      """, nativeQuery = true)
  int countSrsDueFollowed(@Param("userId") Long userId, @Param("gameModeId") Long gameModeId);

  @Query(value = """
      SELECT COUNT(*) FROM knowledges k
      WHERE k.user_id = :userId
        AND k.game_mode_id = :gameModeId
        AND k.status = 'LEARNED'
        AND k.global_streak <= 0
        AND k.last_review_date >= CURRENT_TIMESTAMP - INTERVAL 1 DAY
        AND k.person_id IN (SELECT s.person_id FROM user_subscriptions s WHERE s.user_id = :userId)
      """, nativeQuery = true)
  int countRecentErrorsFollowed(@Param("userId") Long userId, @Param("gameModeId") Long gameModeId);

  // --- SRS DUE & RECENT ERRORS (ALL scope) ----------------------

  @Query(value = """
      SELECT COUNT(*) FROM knowledges k
      WHERE k.user_id = :userId
        AND k.game_mode_id = :gameModeId
        AND k.status = 'LEARNED'
        AND k.next_review_date <= CURRENT_TIMESTAMP
      """, nativeQuery = true)
  int countSrsDueAll(@Param("userId") Long userId, @Param("gameModeId") Long gameModeId);

  @Query(value = """
      SELECT COUNT(*) FROM knowledges k
      WHERE k.user_id = :userId
        AND k.game_mode_id = :gameModeId
        AND k.status = 'LEARNED'
        AND k.global_streak <= 0
        AND k.last_review_date >= CURRENT_TIMESTAMP - INTERVAL 1 DAY
      """, nativeQuery = true)
  int countRecentErrorsAll(@Param("userId") Long userId, @Param("gameModeId") Long gameModeId);
}
