// src/main/java/com/saymyname/persistence/repository/course/CourseQuestionItemRepository.java
package com.saymyname.persistence.repository.course;

import com.saymyname.core.model.enums.course.QuizQuestionItemRole;
import com.saymyname.persistence.entity.organization.course.CourseQuestionItemEntity;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseQuestionItemRepository extends JpaRepository<CourseQuestionItemEntity, Long> {

      /** Update ciblé des items TARGET (tenant guard explicite). */
      @Modifying(clearAutomatically = true, flushAutomatically = true)
      @Query("""
                  UPDATE CourseQuestionItemEntity i
                     SET i.answered = :answered,
                         i.correct = :correct,
                         i.normalizedAnswer = :normalizedAnswer
                   WHERE i.attempt.id = :attemptId
                     AND i.role = :role
                     AND i.tenantId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
                  """)
      int updateItemsAnswerMeta(
                  @Param("attemptId") Long attemptId,
                  @Param("answered") boolean answered,
                  @Param("correct") Boolean correct,
                  @Param("normalizedAnswer") String normalizedAnswer,
                  @Param("role") QuizQuestionItemRole role);
}