package com.saymyname.persistence.repository.course;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.persistence.entity.organization.course.CourseRecentStatsEntity;

@Repository
public interface CourseRecentStatsRepository extends JpaRepository<CourseRecentStatsEntity, Long> {

  @Query("""
      select cs
        from CourseRecentStatsEntity cs
       where cs.course.id = :courseId
         and cs.tenantId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
      """)
  Optional<CourseRecentStatsEntity> findByCourseId(@Param("courseId") Long courseId);

  @Modifying
  @Transactional
  @Query(value = """
      INSERT INTO course_recent_stats
        (tenant_id, course_id, error_streak, help_streak, last_format,
         format_streak, avg_rt_recent, last_answer_at)
      VALUES
        (:#{T(com.saymyname.core.multitenancy.OrgContext).get()}, :courseId,
         :errorStreak, :helpStreak, :lastFormat,
         :formatStreak, :avgRtRecent, :answeredAt)
      ON DUPLICATE KEY UPDATE
        error_streak = CASE
          WHEN :correct = 1 THEN 0
          ELSE COALESCE(error_streak, 0) + 1
        END,
        help_streak = CASE
          WHEN :helpUsed = 1 THEN COALESCE(help_streak, 0) + 1
          ELSE 0
        END,
        format_streak = CASE
          WHEN last_format = :lastFormat THEN COALESCE(format_streak, 0) + 1
          ELSE 1
        END,
        last_format = :lastFormat,
        avg_rt_recent = (COALESCE(avg_rt_recent, 0) * :decay) + (:responseTimeMs * (1 - :decay)),
        last_answer_at = :answeredAt,
        updated_at = NOW()
      """, nativeQuery = true)
  int upsertOnAnswer(
      @Param("courseId") Long courseId,
      @Param("errorStreak") int errorStreak,
      @Param("helpStreak") int helpStreak,
      @Param("lastFormat") String lastFormat,
      @Param("formatStreak") int formatStreak,
      @Param("avgRtRecent") double avgRtRecent,
      @Param("answeredAt") LocalDateTime answeredAt,
      @Param("correct") int correct,
      @Param("helpUsed") int helpUsed,
      @Param("responseTimeMs") int responseTimeMs,
      @Param("decay") double decay);
}
