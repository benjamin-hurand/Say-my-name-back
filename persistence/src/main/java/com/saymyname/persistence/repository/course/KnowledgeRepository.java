// src/main/java/com/saymyname/persistence/repository/course/KnowledgeRepository.java
package com.saymyname.persistence.repository.course;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.course.KnowledgeCandidate;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.persistence.entity.organization.course.KnowledgeEntity;

@Repository
public interface KnowledgeRepository extends JpaRepository<KnowledgeEntity, Long> {

  // ----------------------------------------------------------------
  // UPSERT (SQL natif)
  // ----------------------------------------------------------------

  @Modifying
  @Transactional
  @Query(value = """
        INSERT INTO knowledges
          (tenant_id, user_id, fact_id,
           next_review_date, total_repetition_count,
           srs_streak, global_streak, ease_factor,
           status, last_review_date,
           success_count, failure_count, stability, difficulty,
           pending_revalidation, revalidation_reason)
        VALUES
          (:#{T(com.saymyname.core.multitenancy.TenantContext).get()},
           :userId, :factId,
           :nextReviewDate, :totalCount,
           :srs_streak, :global_streak, :easeFactor,
           :status, :lastReviewDate,
           :successCount, :failureCount, :stability, :difficulty,
           :pendingRevalidation, :revalidationReason)
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
          difficulty                 = VALUES(difficulty),
          pending_revalidation       = VALUES(pending_revalidation),
          revalidation_reason        = VALUES(revalidation_reason)
      """, nativeQuery = true)
  void upsertKnowledge(
      @Param("userId") Long userId,
      @Param("factId") Long factId,
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
      @Param("difficulty") double difficulty,
      @Param("pendingRevalidation") boolean pendingRevalidation,
      @Param("revalidationReason") String revalidationReason);

  // ----------------------------------------------------------------
  // INSERT BATCH (FOLLOWED / ALL)
  // ----------------------------------------------------------------

  @Modifying
  @Transactional
  @Query(value = """
      INSERT IGNORE INTO knowledges (
        tenant_id, user_id, fact_id, status,
        next_review_date, last_review_date, total_repetition_count,
        failure_count, success_count, srs_streak, global_streak,
        ease_factor, difficulty, stability,
        pending_revalidation, revalidation_reason
      )
      SELECT
        :#{T(com.saymyname.core.multitenancy.TenantContext).get()} AS tenant_id,
        :userId AS user_id,
        f.id AS fact_id,
        'UNKNOWN' AS status,
        CURRENT_TIMESTAMP AS next_review_date,
        NULL AS last_review_date,
        0,0,0,0,0,
        :initialEf, :initialDiff, :initialStab,
        0, NULL
      FROM facts f
      JOIN user_subscriptions s
        ON s.tenant_id = f.tenant_id
       AND s.person_id = f.person_id
       AND s.user_id = :userId
      WHERE f.tenant_id = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
        AND (:targetAttributeId IS NULL OR f.attribute_id = :targetAttributeId)
        AND NOT EXISTS (
          SELECT 1
          FROM knowledges k
          WHERE k.tenant_id = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
            AND k.user_id = :userId
            AND k.fact_id = f.id
        )
      ORDER BY RAND()
      LIMIT :limit
      """, nativeQuery = true)
  int insertNextKnowledgesForCourseFollowed(
      @Param("userId") Long userId,
      @Param("targetAttributeId") Long targetAttributeId,
      @Param("initialEf") double initialEaseFactor,
      @Param("initialDiff") double initialDifficuly,
      @Param("initialStab") double initialStability,
      @Param("limit") int limit);

  @Modifying
  @Transactional
  @Query(value = """
      INSERT IGNORE INTO knowledges (
        tenant_id, user_id, fact_id, status,
        next_review_date, last_review_date, total_repetition_count,
        failure_count, success_count, srs_streak, global_streak,
        ease_factor, difficulty, stability,
        pending_revalidation, revalidation_reason
      )
      SELECT
        :#{T(com.saymyname.core.multitenancy.TenantContext).get()} AS tenant_id,
        :userId AS user_id,
        f.id AS fact_id,
        'UNKNOWN' AS status,
        CURRENT_TIMESTAMP AS next_review_date,
        NULL AS last_review_date,
        0,0,0,0,0,
        :initialEf, :initialDiff, :initialStab,
        0, NULL
      FROM facts f
      WHERE f.tenant_id = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
        AND (:targetAttributeId IS NULL OR f.attribute_id = :targetAttributeId)
        AND NOT EXISTS (
          SELECT 1
          FROM knowledges k
          WHERE k.tenant_id = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
            AND k.user_id = :userId
            AND k.fact_id = f.id
        )
      ORDER BY RAND()
      LIMIT :limit
      """, nativeQuery = true)
  int insertNextKnowledgesForCourseAll(
      @Param("userId") Long userId,
      @Param("targetAttributeId") Long targetAttributeId,
      @Param("initialEf") double initialEaseFactor,
      @Param("initialDiff") double initialDifficuly,
      @Param("initialStab") double initialStability,
      @Param("limit") int limit);

  // ----------------------------------------------------------------
  // COMPTAGE
  // ----------------------------------------------------------------

  int countByUserIdAndStatusIn(Long userId, Collection<KnowledgeStatus> statuses);

  @Query("""
        select count(k) from KnowledgeEntity k
         where k.user.id = :userId
           and k.status = com.saymyname.core.model.enums.KnowledgeStatus.LEARNED
           and k.nextReviewDate <= CURRENT_TIMESTAMP
           and (:targetAttributeId is null or k.fact.attributeId = :targetAttributeId)
           and (
             :followed = false
             or exists (
               select 1 from UserSubscriptionEntity s
                where s.userId = :userId
                  and s.personId = k.fact.personId
             )
           )
      """)
  long countSrsDue(@Param("userId") Long userId,
      @Param("targetAttributeId") Long targetAttributeId,
      @Param("followed") boolean followed);

  // ----------------------------------------------------------------
  // SINGLE / LOOKUP
  // ----------------------------------------------------------------

  Optional<KnowledgeEntity> findByUserIdAndFactId(Long userId, Long factId);

  @Query("""
        select k from KnowledgeEntity k
         where k.id = :knowledgeId
           and k.user.id = :userId
           and k.tenantId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
      """)
  Optional<KnowledgeEntity> findByIdForUser(@Param("userId") Long userId,
      @Param("knowledgeId") Long knowledgeId);

  @Query("""
        select k from KnowledgeEntity k
         where k.id in :knowledgeIds
           and k.user.id = :userId
           and k.tenantId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
      """)
  List<KnowledgeEntity> findAllByIdsForUser(@Param("userId") Long userId,
      @Param("knowledgeIds") Collection<Long> knowledgeIds);

  // ----------------------------------------------------------------
  // POOLS FOLLOWED / ALL (JPQL + Pageable limit 1) => KnowledgeCandidate
  // ----------------------------------------------------------------

  @Query("""
        select new com.saymyname.core.model.course.KnowledgeCandidate(
          k.id, k.factId, f.personId, f.attributeId, k.status, k.nextReviewDate
        )
        from KnowledgeEntity k
        join k.fact f
         where k.user.id = :userId
           and k.status = com.saymyname.core.model.enums.KnowledgeStatus.UNKNOWN
           and (:targetAttributeId is null or f.attributeId = :targetAttributeId)
           and ( :allowRepeat = true or f.personId <> :lastPersonId )
           and exists (
              select 1 from UserSubscriptionEntity s
               where s.userId = :userId and s.personId = f.personId
           )
        order by k.id asc
      """)
  List<KnowledgeCandidate> findFirstNewItemFollowed(
      @Param("userId") Long userId,
      @Param("targetAttributeId") Long targetAttributeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat,
      Pageable page);

  @Query("""
        select new com.saymyname.core.model.course.KnowledgeCandidate(
          k.id, k.factId, f.personId, f.attributeId, k.status, k.nextReviewDate
        )
        from KnowledgeEntity k
        join k.fact f
         where k.user.id = :userId
           and k.status = com.saymyname.core.model.enums.KnowledgeStatus.DISCOVERED
           and (:targetAttributeId is null or f.attributeId = :targetAttributeId)
           and ( :allowRepeat = true or f.personId <> :lastPersonId )
           and exists (
              select 1 from UserSubscriptionEntity s
               where s.userId = :userId and s.personId = f.personId
           )
        order by k.id asc
      """)
  List<KnowledgeCandidate> findFirstNotSoNewItemFollowed(
      @Param("userId") Long userId,
      @Param("targetAttributeId") Long targetAttributeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat,
      Pageable page);

  @Query("""
        select new com.saymyname.core.model.course.KnowledgeCandidate(
          k.id, k.factId, f.personId, f.attributeId, k.status, k.nextReviewDate
        )
        from KnowledgeEntity k
        join k.fact f
         where k.user.id = :userId
           and k.status = com.saymyname.core.model.enums.KnowledgeStatus.LEARNED
           and k.globalStreak <= 0
           and k.lastReviewDate >= :since
           and (:targetAttributeId is null or f.attributeId = :targetAttributeId)
           and ( :allowRepeat = true or f.personId <> :lastPersonId )
           and exists (
              select 1 from UserSubscriptionEntity s
               where s.userId = :userId and s.personId = f.personId
           )
        order by k.lastReviewDate asc
      """)
  List<KnowledgeCandidate> findFirstRecentErrorFollowed(
      @Param("userId") Long userId,
      @Param("targetAttributeId") Long targetAttributeId,
      @Param("since") LocalDateTime since,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat,
      Pageable page);

  @Query("""
        select new com.saymyname.core.model.course.KnowledgeCandidate(
          k.id, k.factId, f.personId, f.attributeId, k.status, k.nextReviewDate
        )
        from KnowledgeEntity k
        join k.fact f
         where k.user.id = :userId
           and k.status = com.saymyname.core.model.enums.KnowledgeStatus.LEARNED
           and k.nextReviewDate <= CURRENT_TIMESTAMP
           and (:targetAttributeId is null or f.attributeId = :targetAttributeId)
           and ( :allowRepeat = true or f.personId <> :lastPersonId )
           and exists (
              select 1 from UserSubscriptionEntity s
               where s.userId = :userId and s.personId = f.personId
           )
        order by k.nextReviewDate asc
      """)
  List<KnowledgeCandidate> findFirstSrsDueFollowed(
      @Param("userId") Long userId,
      @Param("targetAttributeId") Long targetAttributeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat,
      Pageable page);

  @Query("""
        select new com.saymyname.core.model.course.KnowledgeCandidate(
          k.id, k.factId, f.personId, f.attributeId, k.status, k.nextReviewDate
        )
        from KnowledgeEntity k
        join k.fact f
         where k.user.id = :userId
           and (
              k.status = com.saymyname.core.model.enums.KnowledgeStatus.MASTERED
              or (
                   k.status = com.saymyname.core.model.enums.KnowledgeStatus.LEARNED
               and k.nextReviewDate > CURRENT_TIMESTAMP
               and not (k.globalStreak <= 0 and k.lastReviewDate >= :since)
              )
           )
           and (:targetAttributeId is null or f.attributeId = :targetAttributeId)
           and ( :allowRepeat = true or f.personId <> :lastPersonId )
           and exists (
              select 1 from UserSubscriptionEntity s
               where s.userId = :userId and s.personId = f.personId
           )
        order by function('rand')
      """)
  List<KnowledgeCandidate> findRevisionFollowed(
      @Param("userId") Long userId,
      @Param("targetAttributeId") Long targetAttributeId,
      @Param("since") LocalDateTime since,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat,
      Pageable page);

  // ---- ALL

  @Query("""
        select new com.saymyname.core.model.course.KnowledgeCandidate(
          k.id, k.factId, f.personId, f.attributeId, k.status, k.nextReviewDate
        )
        from KnowledgeEntity k
        join k.fact f
         where k.user.id = :userId
           and k.status = com.saymyname.core.model.enums.KnowledgeStatus.UNKNOWN
           and (:targetAttributeId is null or f.attributeId = :targetAttributeId)
           and ( :allowRepeat = true or f.personId <> :lastPersonId )
        order by k.id asc
      """)
  List<KnowledgeCandidate> findFirstNewItemAll(
      @Param("userId") Long userId,
      @Param("targetAttributeId") Long targetAttributeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat,
      Pageable page);

  @Query("""
        select new com.saymyname.core.model.course.KnowledgeCandidate(
          k.id, k.factId, f.personId, f.attributeId, k.status, k.nextReviewDate
        )
        from KnowledgeEntity k
        join k.fact f
         where k.user.id = :userId
           and k.status = com.saymyname.core.model.enums.KnowledgeStatus.DISCOVERED
           and (:targetAttributeId is null or f.attributeId = :targetAttributeId)
           and ( :allowRepeat = true or f.personId <> :lastPersonId )
        order by k.id asc
      """)
  List<KnowledgeCandidate> findFirstNotSoNewItemAll(
      @Param("userId") Long userId,
      @Param("targetAttributeId") Long targetAttributeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat,
      Pageable page);

  @Query("""
        select new com.saymyname.core.model.course.KnowledgeCandidate(
          k.id, k.factId, f.personId, f.attributeId, k.status, k.nextReviewDate
        )
        from KnowledgeEntity k
        join k.fact f
         where k.user.id = :userId
           and k.status = com.saymyname.core.model.enums.KnowledgeStatus.LEARNED
           and k.globalStreak <= 0
           and k.lastReviewDate >= :since
           and (:targetAttributeId is null or f.attributeId = :targetAttributeId)
           and ( :allowRepeat = true or f.personId <> :lastPersonId )
        order by k.lastReviewDate asc
      """)
  List<KnowledgeCandidate> findFirstRecentErrorAll(
      @Param("userId") Long userId,
      @Param("targetAttributeId") Long targetAttributeId,
      @Param("since") LocalDateTime since,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat,
      Pageable page);

  @Query("""
        select new com.saymyname.core.model.course.KnowledgeCandidate(
          k.id, k.factId, f.personId, f.attributeId, k.status, k.nextReviewDate
        )
        from KnowledgeEntity k
        join k.fact f
         where k.user.id = :userId
           and k.status = com.saymyname.core.model.enums.KnowledgeStatus.LEARNED
           and k.nextReviewDate <= CURRENT_TIMESTAMP
           and (:targetAttributeId is null or f.attributeId = :targetAttributeId)
           and ( :allowRepeat = true or f.personId <> :lastPersonId )
        order by k.nextReviewDate asc
      """)
  List<KnowledgeCandidate> findFirstSrsDueAll(
      @Param("userId") Long userId,
      @Param("targetAttributeId") Long targetAttributeId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat,
      Pageable page);

  @Query("""
        select new com.saymyname.core.model.course.KnowledgeCandidate(
          k.id, k.factId, f.personId, f.attributeId, k.status, k.nextReviewDate
        )
        from KnowledgeEntity k
        join k.fact f
         where k.user.id = :userId
           and (
              k.status = com.saymyname.core.model.enums.KnowledgeStatus.MASTERED
              or (
                   k.status = com.saymyname.core.model.enums.KnowledgeStatus.LEARNED
               and k.nextReviewDate > CURRENT_TIMESTAMP
               and not (k.globalStreak <= 0 and k.lastReviewDate >= :since)
              )
           )
           and (:targetAttributeId is null or f.attributeId = :targetAttributeId)
           and ( :allowRepeat = true or f.personId <> :lastPersonId )
        order by function('rand')
      """)
  List<KnowledgeCandidate> findRevisionAll(
      @Param("userId") Long userId,
      @Param("targetAttributeId") Long targetAttributeId,
      @Param("since") LocalDateTime since,
      @Param("lastPersonId") Long lastPersonId,
      @Param("allowRepeat") boolean allowRepeat,
      Pageable page);

  // ----------------------------------------------------------------
  // MULTI-TARGET (native) => projection raw
  // ----------------------------------------------------------------

  @Query(value = """
      SELECT
        k.id            AS knowledge_id,
        k.fact_id       AS fact_id,
        f.person_id     AS person_id,
        f.attribute_id  AS attribute_id,
        k.status        AS status,
        k.next_review_date AS next_review_date
      FROM knowledges k
      JOIN facts f
        ON f.tenant_id = k.tenant_id
       AND f.id = k.fact_id
      JOIN (
        SELECT
          k2.id,
          COALESCE(ks2.last_answer_at, '1970-01-01') AS last_answer_sort,
          ROW_NUMBER() OVER (
            PARTITION BY f2.person_id
            ORDER BY k2.next_review_date ASC, COALESCE(ks2.last_answer_at, '1970-01-01') ASC, k2.id ASC
          ) AS rn
        FROM knowledges k2
        JOIN facts f2
          ON f2.tenant_id = k2.tenant_id
         AND f2.id = k2.fact_id
        LEFT JOIN knowledge_stats ks2
          ON ks2.tenant_id = k2.tenant_id
         AND ks2.user_id = k2.user_id
         AND ks2.knowledge_id = k2.id
        WHERE k2.tenant_id = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
          AND k2.user_id = :userId
          AND k2.status IN (:statuses)
          AND (:targetAttributeId IS NULL OR f2.attribute_id = :targetAttributeId)
          AND (:primaryPersonId IS NULL OR f2.person_id <> :primaryPersonId)
          AND (:lastPersonId IS NULL OR f2.person_id <> :lastPersonId)
          AND ( :followed = false OR EXISTS (
              SELECT 1
              FROM user_subscriptions s
              WHERE s.tenant_id = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
                AND s.user_id = :userId
                AND s.person_id = f2.person_id
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
  List<Object[]> findNextDueMultiRaw(
      @Param("userId") Long userId,
      @Param("targetAttributeId") Long targetAttributeId,
      @Param("primaryPersonId") Long primaryPersonId,
      @Param("lastPersonId") Long lastPersonId,
      @Param("statuses") List<String> statuses,
      @Param("followed") boolean followed,
      @Param("maxErrorStreak") int maxErrorStreak,
      @Param("maxAvgRtMs") double maxAvgRtMs,
      @Param("maxHelpRecent") double maxHelpRecent,
      @Param("minAttemptsRecent") double minAttemptsRecent,
      @Param("limit") int limit);

  // ----------------------------------------------------------------
  // LISTE / RESET
  // ----------------------------------------------------------------

  List<KnowledgeEntity> findByUserIdAndStatusNot(Long userId, KnowledgeStatus statusExcluded);

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
               k.stability = :baselineStability,
               k.pendingRevalidation = false,
               k.revalidationReason = null
         where k.user.id = :userId
           and (:targetAttributeId is null or k.fact.attributeId = :targetAttributeId)
           and (
             :popScope = 'ALL'
             or (
               :popScope = 'FOLLOWED'
               and exists (
                 select 1 from UserSubscriptionEntity s
                  where s.userId = :userId
                    and s.personId = k.fact.personId
               )
             )
           )
      """)
  int resetForCourseScope(@Param("userId") long userId,
      @Param("targetAttributeId") Long targetAttributeId,
      @Param("popScope") String popScope, // 'ALL' | 'FOLLOWED'
      @Param("unknown") KnowledgeStatus unknown,
      @Param("baselineEase") double baselineEase,
      @Param("baselineDiff") double baselineDiff,
      @Param("baselineStability") double baselineStability);

  @Query("""
        select count(k) from KnowledgeEntity k
         where k.user.id = :userId
           and (:targetAttributeId is null or k.fact.attributeId = :targetAttributeId)
           and (
             :popScope = 'ALL'
             or (
               :popScope = 'FOLLOWED'
               and exists (
                 select 1 from UserSubscriptionEntity s
                  where s.userId = :userId
                    and s.personId = k.fact.personId
               )
             )
           )
      """)
  long countToResetForCourseScope(@Param("userId") long userId,
      @Param("targetAttributeId") Long targetAttributeId,
      @Param("popScope") String popScope);
}