// src/main/java/com/saymyname/persistence/repository/course/CourseQuestionAttemptRepository.java
package com.saymyname.persistence.repository.course;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saymyname.persistence.entity.organization.course.CourseQuestionAttemptEntity;

@Repository
public interface CourseQuestionAttemptRepository extends JpaRepository<CourseQuestionAttemptEntity, Long> {

  /** ⚠ Bulk JPQL → garde-fou tenant explicite */
  @Modifying
  @Query("""
          DELETE FROM CourseQuestionAttemptEntity c
           WHERE c.course.id = :courseId
             AND c.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
      """)
  void deleteByCourseId(@Param("courseId") Long courseId);

  /** Fetch complet (attempt + items) avec tenant guard. */
  @EntityGraph(attributePaths = { "items" })
  @Query("""
          SELECT c
            FROM CourseQuestionAttemptEntity c
           WHERE c.id = :id
             AND c.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
      """)
  Optional<CourseQuestionAttemptEntity> findByIdWithItems(@Param("id") Long id);

  /** Count avec tenant guard. */
  @Query("""
          SELECT COUNT(c)
            FROM CourseQuestionAttemptEntity c
           WHERE c.course.id = :courseId
             AND c.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
      """)
  long countByCourseIdTenant(@Param("courseId") Long courseId);

  @Modifying
  @Query("""
          UPDATE CourseQuestionAttemptEntity c
             SET c.stepStateJson = :stepStateJson,
                 c.rawSubmission = :rawSubmission,
                 c.normalizedAudit = :normalizedAudit
           WHERE c.id = :id
             AND c.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
      """)
  int updateStepStateAndAudit(
      @Param("id") Long id,
      @Param("stepStateJson") String stepStateJson,
      @Param("rawSubmission") String rawSubmission,
      @Param("normalizedAudit") String normalizedAudit);

  /** Count depuis date avec tenant guard. */
  @Query("""
          SELECT COUNT(c)
            FROM CourseQuestionAttemptEntity c
           WHERE c.course.id = :courseId
             AND c.answeredAt > :after
             AND c.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
      """)
  long countByCourseIdAndAnsweredAtAfterTenant(@Param("courseId") Long courseId, @Param("after") LocalDateTime after);

  /** Dernière activité avec tenant guard. */
  @Query("""
          select max(c.answeredAt)
            from CourseQuestionAttemptEntity c
           where c.course.id = :courseId
             and c.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
      """)
  LocalDateTime findLastAnsweredAt(@Param("courseId") Long courseId);

  interface RecentAnswerRow {
    boolean isGlobalCorrect();
    LocalDateTime getAnsweredAt();
  }

  @Query("""
          SELECT c.globalCorrect as globalCorrect, c.answeredAt as answeredAt
            FROM CourseQuestionAttemptEntity c
           WHERE c.course.id = :courseId
             AND c.answeredAt IS NOT NULL
             AND c.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
           ORDER BY c.answeredAt DESC
      """)
  List<RecentAnswerRow> findRecentAnsweredRows(@Param("courseId") Long courseId, Pageable pageable);

  @Modifying
  @Query("""
        UPDATE CourseQuestionAttemptEntity c
           SET c.helpUsed = true
         WHERE c.id = :id
           AND c.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
      """)
  int markHelpUsed(@Param("id") Long id);

  @Modifying
  @Query("""
        UPDATE CourseQuestionAttemptEntity c
           SET c.answeredAt = :answeredAt,
               c.responseTimeMs = :rt,
               c.rawSubmission = :raw,
               c.normalizedAudit = :norm,
               c.globalCorrect = :globalCorrect
         WHERE c.id = :id
           AND c.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
      """)
  int updateAnswerMeta(
      @Param("id") Long id,
      @Param("answeredAt") LocalDateTime answeredAt,
      @Param("rt") Integer rt,
      @Param("raw") String raw,
      @Param("norm") String norm,
      @Param("globalCorrect") boolean globalCorrect);

  @Modifying
  @Query("""
        UPDATE CourseQuestionAttemptEntity c
           SET c.answeredAt = :answeredAt
         WHERE c.id = :id
           AND c.answeredAt IS NULL
           AND c.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
      """)
  int markAnsweredAtIfNull(
      @Param("id") Long id,
      @Param("answeredAt") LocalDateTime answeredAt);
}
