package com.saymyname.persistence.repository.course;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saymyname.core.model.enums.PopulationScope;
import com.saymyname.core.model.enums.course.CourseStatus;
import com.saymyname.persistence.entity.organization.course.CourseEntity;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {

    Optional<CourseEntity> findFirstByUserIdAndStatus(Long userId, CourseStatus status);

    Optional<CourseEntity> findFirstByUserIdAndTargetAttributeIdAndPopulationScopeAndStatus(
            Long userId, Long targetAttributeId, PopulationScope scope,
            CourseStatus status);

    List<CourseEntity> findByUserId(Long userId);

    List<CourseEntity> findByUserIdAndStatusInOrderByIdDesc(Long userId, Collection<CourseStatus> statuses);

    /** Dernier “focus” (lastAccessedAt) parmi un ensemble de statuts. */
    Optional<CourseEntity> findFirstByUserIdAndStatusInOrderByLastAccessedAtDesc(
            Long userId, Collection<CourseStatus> statuses);

    /** Tous les cours actifs triés par lastAccessedAt (nulls en fin). */
    List<CourseEntity> findByUserIdAndStatusInOrderByLastAccessedAtDesc(Long userId,
            Collection<CourseStatus> statuses);

    /** Fallback si lastAccessedAt est null : tri par updatedAt desc. */
    List<CourseEntity> findByUserIdAndStatusInOrderByUpdatedAtDesc(Long userId, Collection<CourseStatus> statuses);

    /** ⚠ Bulk JPQL → ajout du garde-fou tenant explicite. */
    @Modifying
    @Query("""
            UPDATE CourseEntity c
               SET c.lastAccessedAt = :at
             WHERE c.id = :courseId
               AND c.tenantId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
            """)
    int touchLastAccessed(@Param("courseId") Long courseId, @Param("at") LocalDateTime at);

    @Query("""
             SELECT c FROM CourseEntity c
              WHERE c.user.id = :userId
                AND c.status IN :statuses
                AND c.lastAccessedAt IS NOT NULL
              ORDER BY c.lastAccessedAt DESC
            """)
    List<CourseEntity> findAllActiveWithLastAccess(@Param("userId") Long userId,
            @Param("statuses") Collection<CourseStatus> statuses);
}
