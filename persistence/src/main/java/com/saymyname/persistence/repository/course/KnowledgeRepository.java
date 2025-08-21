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

  // --- UPSERTS (inchangés) -----------------------------------------

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

  // --- Repository
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
        pa.person_id   AS person_id,
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
      FROM course_populations cp
      JOIN populations p
        ON cp.population_id = p.id
      JOIN persons_attributes pa
        ON pa.attribute_id = p.attribute_filter_id
       AND pa.value BETWEEN p.min_value AND p.max_value
       AND pa.valid_from <= CURRENT_TIMESTAMP
       AND (pa.valid_to IS NULL OR pa.valid_to >= CURRENT_TIMESTAMP)
      JOIN persons_attributes pa_sort
        ON pa_sort.person_id   = pa.person_id
       AND pa_sort.attribute_id = :sortingAttributeId
       AND pa_sort.valid_from   <= CURRENT_TIMESTAMP
       AND (pa_sort.valid_to IS NULL OR pa_sort.valid_to >= CURRENT_TIMESTAMP)
      WHERE cp.course_id = :courseId
        AND pa.person_id NOT IN (
          SELECT k.person_id
          FROM knowledges k
          WHERE k.user_id = :userId
            AND k.game_mode_id = :gameModeId
        )
      GROUP BY pa.person_id
      ORDER BY
        CASE WHEN :sortingOrder = 'ASC'  THEN MIN(pa_sort.value) END ASC,
        CASE WHEN :sortingOrder = 'DESC' THEN MIN(pa_sort.value) END DESC
      LIMIT :limit
      """, nativeQuery = true)
  int insertNextKnowledgesForCourse(
      @Param("courseId") Long courseId,
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("sortingAttributeId") Long sortingAttributeId,
      @Param("sortingOrder") String sortingOrder,
      @Param("initialEf") double initialEaseFactor,
      @Param("initialDiff") double initialDifficuly,
      @Param("initialStab") double initialStability,
      @Param("limit") int limit);

  // --- COMPTAGE ----------------------------------------------------

  int countByUserIdAndGameModeIdAndStatusIn(
      Long userId,
      Long gameModeId,
      Collection<KnowledgeStatus> statuses);

  // --- FALLBACK / SINGLE ------------------------------------------------

  /** Récupérer la connaissance précise pour upsertAnswer */
  Optional<KnowledgeEntity> findByUserIdAndGameModeIdAndPersonId(
      Long userId, Long gameModeId, Long personId);

  // --- POOLS SPÉCIFIQUES (LIMIT 1) ---------------------------------------

  /** 1) Nouveaux (status = UNKNOWN) */
  @Query(value = """
      SELECT k.*
        FROM knowledges k
       WHERE k.user_id       = :userId
         AND k.game_mode_id  = :gameModeId
         AND k.status        = 'UNKNOWN'
         -- on n’autorise que les person_id du course
         AND k.person_id IN (
           SELECT pa.person_id
             FROM course_populations cp
             JOIN populations p
               ON cp.population_id = p.id
             JOIN persons_attributes pa
               ON pa.attribute_id = p.attribute_filter_id
              AND pa.value BETWEEN p.min_value AND p.max_value
              AND pa.valid_from <= CURRENT_TIMESTAMP
              AND (pa.valid_to IS NULL OR pa.valid_to >= CURRENT_TIMESTAMP)
            WHERE cp.course_id = :courseId
           GROUP BY pa.person_id
         )
         AND ( :allowRepeat = true OR k.person_id <> :lastPersonId )
       ORDER BY k.id ASC
       LIMIT 1
      """, nativeQuery = true)
  KnowledgeEntity findFirstNewItem(
      @Param("courseId") Long courseId,
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat);

  /** 2) Découvertes (status = DISCOVERED) dans le scope du course */
  @Query(value = """
      SELECT k.*
        FROM knowledges k
       WHERE k.user_id       = :userId
         AND k.game_mode_id  = :gameModeId
         AND k.status        = 'DISCOVERED'
         AND k.person_id IN (
           SELECT pa.person_id
             FROM course_populations cp
             JOIN populations p
               ON cp.population_id = p.id
             JOIN persons_attributes pa
               ON pa.attribute_id = p.attribute_filter_id
              AND pa.value BETWEEN p.min_value AND p.max_value
              AND pa.valid_from <= CURRENT_TIMESTAMP
              AND (pa.valid_to IS NULL OR pa.valid_to >= CURRENT_TIMESTAMP)
            WHERE cp.course_id = :courseId
           GROUP BY pa.person_id
         )
         AND ( :allowRepeat = true OR k.person_id <> :lastPersonId )
       ORDER BY k.id ASC
       LIMIT 1
      """, nativeQuery = true)
  KnowledgeEntity findFirstNotSoNewItem(
      @Param("courseId") Long courseId,
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat);

  // POOLS LEARNED
  /**
   * 3a) Échecs récents (status = LEARNED & erreurs < 24h) dans le scope du course
   */
  @Query(value = """
      SELECT k.*
        FROM knowledges k
       WHERE k.user_id          = :userId
         AND k.game_mode_id     = :gameModeId
         AND k.status           = 'LEARNED'
         AND k.global_streak <= 0
         AND k.last_review_date >= CURRENT_TIMESTAMP - INTERVAL 1 DAY
         AND k.person_id IN (
           SELECT pa.person_id
             FROM course_populations cp
             JOIN populations p
               ON cp.population_id = p.id
             JOIN persons_attributes pa
               ON pa.attribute_id = p.attribute_filter_id
              AND pa.value BETWEEN p.min_value AND p.max_value
              AND pa.valid_from <= CURRENT_TIMESTAMP
              AND (pa.valid_to IS NULL OR pa.valid_to >= CURRENT_TIMESTAMP)
            WHERE cp.course_id = :courseId
           GROUP BY pa.person_id
         )
         AND ( :allowRepeat = true OR k.person_id <> :lastPersonId )
       ORDER BY k.last_review_date ASC
       LIMIT 1
      """, nativeQuery = true)
  KnowledgeEntity findFirstRecentError(
      @Param("courseId") Long courseId,
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat);

  /** 3b) SRS dues (next_review_date ≤ now) dans le scope du course */
  @Query(value = """
      SELECT k.*
        FROM knowledges k
       WHERE k.user_id           = :userId
         AND k.game_mode_id      = :gameModeId
         AND k.status            = 'LEARNED'
         AND k.next_review_date <= CURRENT_TIMESTAMP
         AND k.person_id IN (
           SELECT pa.person_id
             FROM course_populations cp
             JOIN populations p
               ON cp.population_id = p.id
             JOIN persons_attributes pa
               ON pa.attribute_id = p.attribute_filter_id
              AND pa.value BETWEEN p.min_value AND p.max_value
              AND pa.valid_from <= CURRENT_TIMESTAMP
              AND (pa.valid_to IS NULL OR pa.valid_to >= CURRENT_TIMESTAMP)
            WHERE cp.course_id = :courseId
           GROUP BY pa.person_id
         )
         AND ( :allowRepeat = true OR k.person_id <> :lastPersonId )
       ORDER BY k.next_review_date ASC
       LIMIT 1
      """, nativeQuery = true)
  KnowledgeEntity findFirstSrsDue(
      @Param("courseId") Long courseId,
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat);

  /** 3c) Future SRS dues (next_review_date > now) dans le scope du course */
  @Query(value = """
      SELECT k.*
        FROM knowledges k
       WHERE k.user_id           = :userId
         AND k.game_mode_id      = :gameModeId
         AND k.status            = 'LEARNED'
         AND k.next_review_date > CURRENT_TIMESTAMP
         AND NOT (
           k.global_streak <= 0
           AND k.last_review_date >= CURRENT_TIMESTAMP - INTERVAL 1 DAY
         )
         AND k.person_id IN (
           SELECT pa.person_id
             FROM course_populations cp
             JOIN populations p
               ON cp.population_id = p.id
             JOIN persons_attributes pa
               ON pa.attribute_id = p.attribute_filter_id
              AND pa.value BETWEEN p.min_value AND p.max_value
              AND pa.valid_from <= CURRENT_TIMESTAMP
              AND (pa.valid_to IS NULL OR pa.valid_to >= CURRENT_TIMESTAMP)
            WHERE cp.course_id = :courseId
           GROUP BY pa.person_id
         )
         AND ( :allowRepeat = true OR k.person_id <> :lastPersonId )
       ORDER BY RAND()
       LIMIT 1
      """, nativeQuery = true)
  KnowledgeEntity findRandomFutureSrsDue(
      @Param("courseId") Long courseId,
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat);

  /** 4) Random Mastered (status = MASTERED) dans le scope du course */
  @Query(value = """
      SELECT k.*
        FROM knowledges k
       WHERE k.user_id       = :userId
         AND k.game_mode_id  = :gameModeId
         AND k.status        = 'MASTERED'
         AND k.person_id IN (
           SELECT pa.person_id
             FROM course_populations cp
             JOIN populations p
               ON cp.population_id = p.id
             JOIN persons_attributes pa
               ON pa.attribute_id = p.attribute_filter_id
              AND pa.value BETWEEN p.min_value AND p.max_value
              AND pa.valid_from <= CURRENT_TIMESTAMP
              AND (pa.valid_to IS NULL OR pa.valid_to >= CURRENT_TIMESTAMP)
            WHERE cp.course_id = :courseId
           GROUP BY pa.person_id
         )
         AND ( :allowRepeat = true OR k.person_id <> :lastPersonId )
       ORDER BY RAND()
       LIMIT 1
      """, nativeQuery = true)
  KnowledgeEntity findRandomMastered(
      @Param("courseId") Long courseId,
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat);

  /**
   * 3c & 4) Random review: MASTERED ou LEARNED éligible dans le scope du course
   */
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
           SELECT pa.person_id
             FROM course_populations cp
             JOIN populations p
               ON cp.population_id = p.id
             JOIN persons_attributes pa
               ON pa.attribute_id = p.attribute_filter_id
              AND pa.value BETWEEN p.min_value AND p.max_value
              AND pa.valid_from <= CURRENT_TIMESTAMP
              AND (pa.valid_to IS NULL OR pa.valid_to >= CURRENT_TIMESTAMP)
            WHERE cp.course_id = :courseId
           GROUP BY pa.person_id
         )
         AND ( :allowRepeat = true OR k.person_id <> :lastPersonId )
       ORDER BY RAND()
       LIMIT 1
      """, nativeQuery = true)
  KnowledgeEntity findRevision(
      @Param("courseId") Long courseId,
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat);

  /**
   * Renvoie tous les knowledges pour cet user + gameMode,
   * dont le status n'est pas MASTERED.
   */
  List<KnowledgeEntity> findByGameModeIdAndUserIdAndStatusNot(
      Long gameModeId,
      Long userId,
      KnowledgeStatus statusExcluded);

}
