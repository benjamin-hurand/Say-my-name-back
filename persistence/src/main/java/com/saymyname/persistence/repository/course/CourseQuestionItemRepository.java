// src/main/java/com/saymyname/persistence/repository/course/CourseQuestionItemRepository.java
package com.saymyname.persistence.repository.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saymyname.persistence.entity.organization.course.CourseQuestionItemEntity;

@Repository
public interface CourseQuestionItemRepository extends JpaRepository<CourseQuestionItemEntity, Long> {

   /** Update ciblķ des items TARGET (tenant guard explicite). */
   @Modifying
   @Query("""
         UPDATE CourseQuestionItemEntity i
            SET i.answered = :answered,
                i.correct = :correct,
                i.normalizedAnswer = :normalizedAnswer
          WHERE i.attempt.id = :attemptId
            AND i.role = com.saymyname.persistence.entity.organization.course.CourseQuestionItemEntity.Role.TARGET
            AND i.tenantId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
         """)
   int updateTargetItemsAnswerMeta(
         @Param("attemptId") Long attemptId,
         @Param("answered") boolean answered,
         @Param("correct") Boolean correct,
         @Param("normalizedAnswer") String normalizedAnswer);
}
