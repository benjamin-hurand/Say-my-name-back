// src/main/java/com/saymyname/persistence/repository/course/CourseQuestionHistoryRepository.java
package com.saymyname.persistence.repository.course;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saymyname.persistence.entity.organization.course.CourseQuestionHistoryEntity;

@Repository
public interface CourseQuestionHistoryRepository extends JpaRepository<CourseQuestionHistoryEntity, Long> {

  /** ⚠ Bulk JPQL → garde-fou tenant explicite */
  @Modifying
  @Query("""
          DELETE FROM CourseQuestionHistoryEntity c
           WHERE c.course.id = :courseId
             AND c.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
      """)
  void deleteByCourseId(@Param("courseId") Long courseId);

  /** Fetch complet (history + items) avec tenant guard. */
  @EntityGraph(attributePaths = { "items" })
  @Query("""
          SELECT c
            FROM CourseQuestionHistoryEntity c
           WHERE c.id = :id
             AND c.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
      """)
  Optional<CourseQuestionHistoryEntity> findByIdWithItems(@Param("id") Long id);

  /** Count avec tenant guard. */
  @Query("""
          SELECT COUNT(c)
            FROM CourseQuestionHistoryEntity c
           WHERE c.course.id = :courseId
             AND c.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
      """)
  long countByCourseIdTenant(@Param("courseId") Long courseId);

  /** Count depuis date avec tenant guard. */
  @Query("""
          SELECT COUNT(c)
            FROM CourseQuestionHistoryEntity c
           WHERE c.course.id = :courseId
             AND c.answeredAt > :after
             AND c.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
      """)
  long countByCourseIdAndAnsweredAtAfterTenant(@Param("courseId") Long courseId, @Param("after") LocalDateTime after);

  /** Dernière activité avec tenant guard. */
  @Query("""
          select max(c.answeredAt)
            from CourseQuestionHistoryEntity c
           where c.course.id = :courseId
             and c.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
      """)
  LocalDateTime findLastAnsweredAt(@Param("courseId") Long courseId);

  @Modifying
  @Query("""
        UPDATE CourseQuestionHistoryEntity c
           SET c.helpUsed = true
         WHERE c.id = :id
           AND c.organizationId = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
      """)
  int markHelpUsed(@Param("id") Long id);

  @Modifying
  @Query("""
        UPDATE CourseQuestionHistoryEntity c
           SET c.answeredAt = :answeredAt,
               c.responseTimeMs = :rt,
               c.rawSubmission = :raw,
               c.normalizedSubmission = :norm,
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
}
