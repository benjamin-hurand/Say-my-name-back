package com.saymyname.persistence.repository.course;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.persistence.entity.organization.course.KnowledgeEntity;

@Repository
public interface KnowledgeRepository extends JpaRepository<KnowledgeEntity, Long> {

  // ---- UPSERT (SQL natif : on garde le SpEL orgId) ----------------

  @Modifying
  @Transactional
  @Query(value = """
        INSERT INTO knowledges
          (user_id, game_mode_id, person_id, organization_id,
           next_review_date, total_repetition_count,
           srs_streak, global_streak, ease_factor,
           status, last_review_date,
           success_count, failure_count, stability, difficulty)
        VALUES
          (:userId, :gameModeId, :personId,
           :#{T(com.saymyname.core.multitenancy.OrgContext).get()},
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

  // ---- INSERT BATCH (FOLLOWED) ------------------------------------

  @Modifying
  @Transactional
  @Query(value = """
      INSERT IGNORE INTO knowledges (
        user_id, game_mode_id, person_id, organization_id, status,
        next_review_date, last_review_date, total_repetition_count,
        failure_count, success_count, srs_streak, global_streak,
        ease_factor, difficulty, stability
      )
      SELECT
        :userId        AS user_id,
        :gameModeId    AS game_mode_id,
        s.person_id    AS person_id,
        :#{T(com.saymyname.core.multitenancy.OrgContext).get()} AS organization_id,
        'UNKNOWN'      AS status,
        CURRENT_TIMESTAMP AS next_review_date,
        NULL           AS last_review_date,
        0, 0, 0, 0, 0,
        :initialEf, :initialDiff, :initialStab
      FROM user_subscriptions s
      WHERE s.user_id = :userId
        AND s.organization_id = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
        AND NOT EXISTS (
          SELECT 1
          FROM knowledges k
          WHERE k.organization_id = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
            AND k.user_id = :userId
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

  // ---- INSERT BATCH (ALL) ----------------------------------------

  @Modifying
  @Transactional
  @Query(value = """
      INSERT IGNORE INTO knowledges (
        user_id, game_mode_id, person_id, organization_id, status,
        next_review_date, last_review_date, total_repetition_count,
        failure_count, success_count, srs_streak, global_streak,
        ease_factor, difficulty, stability
      )
      SELECT
        :userId, :gameModeId, p.id,
        :#{T(com.saymyname.core.multitenancy.OrgContext).get()},
        'UNKNOWN', CURRENT_TIMESTAMP, NULL,
        0,0,0,0,0, :initialEf, :initialDiff, :initialStab
      FROM persons p
      WHERE p.organization_id = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
        AND NOT EXISTS (
          SELECT 1
          FROM knowledges k
          WHERE k.organization_id = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
            AND k.user_id = :userId
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

  // ---- COMPTAGE ---------------------------------------------------

  int countByUserIdAndGameModeIdAndStatusIn(
      Long userId,
      Long gameModeId,
      Collection<KnowledgeStatus> statuses);

  @Query("""
        select count(k) from KnowledgeEntity k
         where k.user.id = :userId
           and k.gameMode.id = :gameModeId
           and k.status = com.saymyname.core.model.enums.KnowledgeStatus.LEARNED
           and k.nextReviewDate <= CURRENT_TIMESTAMP
           and (
             :followed = false
             or exists (
               select 1 from UserSubscriptionEntity s
                where s.id.userId = :userId
                  and s.id.personId = k.person.id
             )
           )
      """)
  long countSrsDue(@Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("followed") boolean followed);

  // ---- FALLBACK / SINGLE -----------------------------------------

  Optional<KnowledgeEntity> findByUserIdAndGameModeIdAndPersonId(
      Long userId, Long gameModeId, Long personId);

  @Query("""
        select k from KnowledgeEntity k
         where k.id = :knowledgeId
           and k.user.id = :userId
           and k.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
      """)
  Optional<KnowledgeEntity> findByIdForUser(
      @Param("userId") Long userId,
      @Param("knowledgeId") Long knowledgeId);

  @Query("""
        select k from KnowledgeEntity k
         where k.id in :knowledgeIds
           and k.user.id = :userId
           and k.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
      """)
  List<KnowledgeEntity> findAllByIdsForUser(
      @Param("userId") Long userId,
      @Param("knowledgeIds") Collection<Long> knowledgeIds);

  // ---- POOLS FOLLOWED (JPQL + Pageable limit 1) -------------------

  @Query("""
        select k from KnowledgeEntity k
         where k.user.id = :userId
           and k.gameMode.id = :gameModeId
           and k.status = com.saymyname.core.model.enums.KnowledgeStatus.UNKNOWN
           and ( :allowRepeat = true or k.person.id <> :lastPersonId )
           and exists (
              select 1 from UserSubscriptionEntity s
               where s.id.userId = :userId and s.id.personId = k.person.id
           )
        order by k.id asc
      """)
  List<KnowledgeEntity> findFirstNewItemFollowed(@Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat,
      Pageable page);

  @Query("""
        select k from KnowledgeEntity k
         where k.user.id = :userId
           and k.gameMode.id = :gameModeId
           and k.status = com.saymyname.core.model.enums.KnowledgeStatus.DISCOVERED
           and ( :allowRepeat = true or k.person.id <> :lastPersonId )
           and exists (
              select 1 from UserSubscriptionEntity s
               where s.id.userId = :userId and s.id.personId = k.person.id
           )
        order by k.id asc
      """)
  List<KnowledgeEntity> findFirstNotSoNewItemFollowed(@Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat,
      Pageable page);

  @Query("""
        select k from KnowledgeEntity k
         where k.user.id = :userId
           and k.gameMode.id = :gameModeId
           and k.status = com.saymyname.core.model.enums.KnowledgeStatus.LEARNED
           and k.globalStreak <= 0
           and k.lastReviewDate >= :since
           and ( :allowRepeat = true or k.person.id <> :lastPersonId )
           and exists (
              select 1 from UserSubscriptionEntity s
               where s.id.userId = :userId and s.id.personId = k.person.id
           )
        order by k.lastReviewDate asc
      """)
  List<KnowledgeEntity> findFirstRecentErrorFollowed(
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("since") LocalDateTime since,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat,
      Pageable page);

  @Query("""
        select k from KnowledgeEntity k
         where k.user.id = :userId
           and k.gameMode.id = :gameModeId
           and k.status = com.saymyname.core.model.enums.KnowledgeStatus.LEARNED
           and k.nextReviewDate <= CURRENT_TIMESTAMP
           and ( :allowRepeat = true or k.person.id <> :lastPersonId )
           and exists (
              select 1 from UserSubscriptionEntity s
               where s.id.userId = :userId and s.id.personId = k.person.id
           )
        order by k.nextReviewDate asc
      """)
  List<KnowledgeEntity> findFirstSrsDueFollowed(
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat,
      Pageable page);

  @Query("""
        select k from KnowledgeEntity k
         where k.user.id = :userId
           and k.gameMode.id = :gameModeId
           and (
              k.status = com.saymyname.core.model.enums.KnowledgeStatus.MASTERED
              or (
                   k.status = com.saymyname.core.model.enums.KnowledgeStatus.LEARNED
               and k.nextReviewDate > CURRENT_TIMESTAMP
               and not (k.globalStreak <= 0 and k.lastReviewDate >= :since)
              )
           )
           and ( :allowRepeat = true or k.person.id <> :lastPersonId )
           and exists (
              select 1 from UserSubscriptionEntity s
               where s.id.userId = :userId and s.id.personId = k.person.id
           )
        order by function('rand')
      """)
  List<KnowledgeEntity> findRevisionFollowed(
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("since") LocalDateTime since,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat,
      Pageable page);

  // ---- POOLS ALL (JPQL + Pageable limit 1) -----------------------

  @Query("""
        select k from KnowledgeEntity k
         where k.user.id = :userId
           and k.gameMode.id = :gameModeId
           and k.status = com.saymyname.core.model.enums.KnowledgeStatus.UNKNOWN
           and ( :allowRepeat = true or k.person.id <> :lastPersonId )
        order by k.id asc
      """)
  List<KnowledgeEntity> findFirstNewItemAll(
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat,
      Pageable page);

  @Query("""
        select k from KnowledgeEntity k
         where k.user.id = :userId
           and k.gameMode.id = :gameModeId
           and k.status = com.saymyname.core.model.enums.KnowledgeStatus.DISCOVERED
           and ( :allowRepeat = true or k.person.id <> :lastPersonId )
        order by k.id asc
      """)
  List<KnowledgeEntity> findFirstNotSoNewItemAll(
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat,
      Pageable page);

  @Query("""
        select k from KnowledgeEntity k
         where k.user.id = :userId
           and k.gameMode.id = :gameModeId
           and k.status = com.saymyname.core.model.enums.KnowledgeStatus.LEARNED
           and k.globalStreak <= 0
           and k.lastReviewDate >= :since
           and ( :allowRepeat = true or k.person.id <> :lastPersonId )
        order by k.lastReviewDate asc
      """)
  List<KnowledgeEntity> findFirstRecentErrorAll(
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("since") LocalDateTime since,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat,
      Pageable page);

  @Query("""
        select k from KnowledgeEntity k
         where k.user.id = :userId
           and k.gameMode.id = :gameModeId
           and k.status = com.saymyname.core.model.enums.KnowledgeStatus.LEARNED
           and k.nextReviewDate <= CURRENT_TIMESTAMP
           and ( :allowRepeat = true or k.person.id <> :lastPersonId )
        order by k.nextReviewDate asc
      """)
  List<KnowledgeEntity> findFirstSrsDueAll(
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat,
      Pageable page);

  @Query("""
        select k from KnowledgeEntity k
         where k.user.id = :userId
           and k.gameMode.id = :gameModeId
           and (
              k.status = com.saymyname.core.model.enums.KnowledgeStatus.MASTERED
              or (
                   k.status = com.saymyname.core.model.enums.KnowledgeStatus.LEARNED
               and k.nextReviewDate > CURRENT_TIMESTAMP
               and not (k.globalStreak <= 0 and k.lastReviewDate >= :since)
              )
           )
           and ( :allowRepeat = true or k.person.id <> :lastPersonId )
        order by function('rand')
      """)
  List<KnowledgeEntity> findRevisionAll(
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("since") LocalDateTime since,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat,
      Pageable page);

  // ---- LISTE / STATS ---------------------------------------------

  // ---- MULTI-TARGET (dedicated selection) ------------------------

  @Query(value = """
      SELECT k.*
      FROM knowledges k
      JOIN (
        SELECT
          k2.id,
          COALESCE(ks2.last_answer_at, '1970-01-01') AS last_answer_sort,
          ROW_NUMBER() OVER (
            PARTITION BY k2.person_id
            ORDER BY k2.next_review_date ASC, COALESCE(ks2.last_answer_at, '1970-01-01') ASC, k2.id ASC
          ) AS rn
        FROM knowledges k2
        LEFT JOIN knowledge_stats ks2
          ON ks2.organization_id = k2.organization_id
         AND ks2.user_id = k2.user_id
         AND ks2.game_mode_id = k2.game_mode_id
         AND ks2.knowledge_id = k2.id
        WHERE k2.organization_id = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
          AND k2.user_id = :userId
          AND k2.game_mode_id = :gameModeId
          AND k2.status IN (:statuses)
          AND (:primaryPersonId IS NULL OR k2.person_id <> :primaryPersonId)
          AND (:lastPersonId IS NULL OR k2.person_id <> :lastPersonId)
          AND ( :followed = false OR EXISTS (
              SELECT 1
              FROM user_subscriptions s
              WHERE s.organization_id = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
                AND s.user_id = :userId
                AND s.person_id = k2.person_id
          ))
          AND COALESCE(ks2.error_streak, 0) <= :maxErrorStreak
          AND COALESCE(ks2.avg_rt_recent, 0) <= :maxAvgRtMs
          AND COALESCE(ks2.help_recent, 0) <= :maxHelpRecent
          AND COALESCE(ks2.attempts_recent, 0) >= :minAttemptsRecent
      ) ranked ON ranked.id = k.id
      WHERE ranked.rn = 1
      ORDER BY k.next_review_date ASC, ranked.last_answer_sort ASC, k.id ASC
      LIMIT :limit
      """, nativeQuery = true)
  List<KnowledgeEntity> findNextDueMulti(
      @Param("userId") Long userId,
      @Param("gameModeId") Long gameModeId,
      @Param("primaryPersonId") Long primaryPersonId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("statuses") List<String> statuses,
      @Param("followed") boolean followed,
      @Param("maxErrorStreak") int maxErrorStreak,
      @Param("maxAvgRtMs") double maxAvgRtMs,
      @Param("maxHelpRecent") double maxHelpRecent,
      @Param("minAttemptsRecent") double minAttemptsRecent,
      @Param("limit") int limit);

  List<KnowledgeEntity> findByGameModeIdAndUserIdAndStatusNot(
      Long gameModeId,
      Long userId,
      KnowledgeStatus statusExcluded);

  // JPQL → filtre Hibernate OK

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query("""
        update KnowledgeEntity k
           set k.status = :unknown,
               k.nextReviewDate = CURRENT_TIMESTAMP,
               k.lastReviewDate = null,
               k.easeFactor = :baselineEase,
               k.srsStreak = 0,
               k.globalStreak = 0,
               k.totalRepetitionCount = 0,
               k.successCount = 0,
               k.failureCount = 0,
               k.difficulty = :baselineDiff,
               k.stability = :baselineStability
         where k.user.id = :userId
           and k.gameMode.id = :gameModeId
           and (
             :popScope = 'ALL'
             or (
               :popScope = 'FOLLOWED'
               and exists (
                 select 1 from UserSubscriptionEntity s
                  where s.id.userId = :userId
                    and s.id.personId = k.person.id
               )
             )
           )
      """)
  int resetForCourseScope(@Param("userId") long userId,
      @Param("gameModeId") long gameModeId,
      @Param("popScope") String popScope, // 'ALL' | 'FOLLOWED'
      @Param("unknown") com.saymyname.core.model.enums.KnowledgeStatus unknown,
      @Param("baselineEase") double baselineEase,
      @Param("baselineDiff") double baselineDiff,
      @Param("baselineStability") double baselineStability);

  @Query("""
        select count(k) from KnowledgeEntity k
         where k.user.id = :userId
           and k.gameMode.id = :gameModeId
           and (
             :popScope = 'ALL'
             or (
               :popScope = 'FOLLOWED'
               and exists (
                 select 1 from UserSubscriptionEntity s
                  where s.id.userId = :userId
                    and s.id.personId = k.person.id
               )
             )
           )
      """)
  long countToResetForCourseScope(@Param("userId") long userId,
      @Param("gameModeId") long gameModeId,
      @Param("popScope") String popScope);
}
