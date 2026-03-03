// src/main/java/com/saymyname/persistence/repository/leaderboard/LeaderboardStatRepository.java
package com.saymyname.persistence.repository.leaderboard;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saymyname.persistence.entity.organization.leaderboard.LeaderboardStatEntity;

@Repository
public interface LeaderboardStatRepository extends JpaRepository<LeaderboardStatEntity, Long> {

  @Query("""
          select ls
          from LeaderboardStatEntity ls
          join fetch ls.user u
          where u.id = :userId
            and ls.tenantId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
      """)
  Optional<LeaderboardStatEntity> findByUserId(@Param("userId") Long userId);

  /**
   * UPSERT perf MySQL : insert si absent, sinon :
   * - xp = xp + delta
   * - total_answers += 1
   * - correct_answers += correctDelta
   * - last_answer_at = max(last_answer_at, eventAt)
   * - updated_at = now()
   */
  @Modifying
  @Query(value = """
          INSERT INTO leaderboard_stats
            (user_id, xp, total_answers, correct_answers, last_answer_at, updated_at, tenant_id)
          VALUES
            (:userId, :deltaXp, :totalAnswersDelta, :correctAnswersDelta, :eventAt, NOW(),
             :#{T(com.saymyname.core.multitenancy.OrgContext).get()})
          ON DUPLICATE KEY UPDATE
            xp = xp + VALUES(xp),
            total_answers = total_answers + VALUES(total_answers),
            correct_answers = correct_answers + VALUES(correct_answers),
            last_answer_at = CASE
                WHEN last_answer_at IS NULL THEN VALUES(last_answer_at)
                WHEN VALUES(last_answer_at) IS NULL THEN last_answer_at
                WHEN VALUES(last_answer_at) > last_answer_at THEN VALUES(last_answer_at)
                ELSE last_answer_at
            END,
            updated_at = NOW()
      """, nativeQuery = true)
  int upsertAddXpAndCounters(
      @Param("userId") Long userId,
      @Param("deltaXp") long deltaXp,
      @Param("totalAnswersDelta") long totalAnswersDelta,
      @Param("correctAnswersDelta") long correctAnswersDelta,
      @Param("eventAt") LocalDateTime eventAt);

  /**
   * Top N : rank via window function (MySQL 8+).
   * tie-break: last_answer_at puis user_id
   */
  @Query(value = """
          SELECT
            u.id as userId,
            u.display_name as displayName,
            ls.xp as xp,
            ls.last_answer_at as lastAnswerAt,
            ROW_NUMBER() OVER (ORDER BY ls.xp DESC, ls.last_answer_at DESC, u.id ASC) as rowNum
          FROM leaderboard_stats ls
          JOIN users u ON u.id = ls.user_id
          WHERE ls.tenant_id = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
          ORDER BY ls.xp DESC, ls.last_answer_at DESC, u.id ASC
          LIMIT :limit
      """, nativeQuery = true)
  List<LeaderboardTopRow> findTop(@Param("limit") int limit);

  /**
   * Rank pour un user : 1 + count des users qui sont "devant"
   * tie-break: last_answer_at puis user_id
   */
  @Query(value = """
          SELECT 1 + COUNT(*)
          FROM leaderboard_stats other
          JOIN leaderboard_stats me
            ON me.user_id = :userId
           AND me.tenant_id = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
          WHERE other.tenant_id = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
            AND (
                  other.xp > me.xp
               OR (other.xp = me.xp AND COALESCE(other.last_answer_at, '1970-01-01') > COALESCE(me.last_answer_at, '1970-01-01'))
               OR (other.xp = me.xp AND COALESCE(other.last_answer_at, '1970-01-01') = COALESCE(me.last_answer_at, '1970-01-01') AND other.user_id < me.user_id)
            )
      """, nativeQuery = true)
  long computeRank(@Param("userId") Long userId);

  interface LeaderboardTopRow {
    Long getUserId();

    String getDisplayName();

    Long getXp();

    LocalDateTime getLastAnswerAt();

    // Alias SQL: "rowNum"
    long getRowNum();
  }
}
