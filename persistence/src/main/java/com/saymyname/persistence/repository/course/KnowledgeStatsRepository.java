package com.saymyname.persistence.repository.course;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.persistence.entity.organization.course.KnowledgeStatsEntity;

@Repository
public interface KnowledgeStatsRepository extends JpaRepository<KnowledgeStatsEntity, Long> {

    @Query("""
            select ks
              from KnowledgeStatsEntity ks
             where ks.user.id = :userId
               and ks.gameMode.id = :gameModeId
               and ks.knowledge.id in :knowledgeIds
               and ks.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
            """)
    List<KnowledgeStatsEntity> findByUserGameModeAndKnowledgeIds(
            @Param("userId") Long userId,
            @Param("gameModeId") Long gameModeId,
            @Param("knowledgeIds") Collection<Long> knowledgeIds);

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO knowledge_stats
              (organization_id, user_id, game_mode_id, knowledge_id, person_id,
               attempts_recent, correct_recent, help_recent, avg_rt_recent,
               last_answer_at, last_correct, last_help_used, last_response_time_ms, error_streak)
            VALUES
              (:#{T(com.saymyname.core.multitenancy.OrgContext).get()}, :userId, :gameModeId, :knowledgeId, :personId,
               :attemptsRecent, :correctRecent, :helpRecent, :avgRtRecent,
               :answeredAt, :lastCorrect, :lastHelpUsed, :responseTimeMs, :errorStreak)
            ON DUPLICATE KEY UPDATE
              attempts_recent = (COALESCE(attempts_recent, 0) * :decay) + 1,
              correct_recent = (COALESCE(correct_recent, 0) * :decay) + :correctRecent,
              help_recent = (COALESCE(help_recent, 0) * :decay) + :helpRecent,
              avg_rt_recent = (COALESCE(avg_rt_recent, 0) * :decay) + (:responseTimeMs * (1 - :decay)),
              last_answer_at = :answeredAt,
              last_correct = :lastCorrect,
              last_help_used = :lastHelpUsed,
              last_response_time_ms = :responseTimeMs,
              error_streak = CASE
                WHEN :lastCorrect = 1 THEN 0
                ELSE COALESCE(error_streak, 0) + 1
              END,
              updated_at = NOW()
            """, nativeQuery = true)
    int upsertOnAnswer(
            @Param("userId") Long userId,
            @Param("gameModeId") Long gameModeId,
            @Param("knowledgeId") Long knowledgeId,
            @Param("personId") Long personId,
            @Param("attemptsRecent") double attemptsRecent,
            @Param("correctRecent") double correctRecent,
            @Param("helpRecent") double helpRecent,
            @Param("avgRtRecent") double avgRtRecent,
            @Param("answeredAt") LocalDateTime answeredAt,
            @Param("lastCorrect") int lastCorrect,
            @Param("lastHelpUsed") int lastHelpUsed,
            @Param("responseTimeMs") int responseTimeMs,
            @Param("errorStreak") int errorStreak,
            @Param("decay") double decay);
}
