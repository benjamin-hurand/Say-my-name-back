// src/main/java/com/saymyname/persistence/repository/leaderboard/XpEventRepository.java
package com.saymyname.persistence.repository.leaderboard;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saymyname.persistence.entity.organization.leaderboard.XpEventEntity;

@Repository
public interface XpEventRepository extends JpaRepository<XpEventEntity, Long> {

    /**
     * History "me" (cursor): items strictement avant (:beforeCreatedAt, :beforeId)
     * Tri DESC stable.
     *
     * JPQL + org filter via Hibernate (BaseOrgScoped).
     */
    @Query("""
            select
              e.id as id,
              e.eventId as eventId,
              e.eventKey as eventKey,
              e.sourceType as sourceType,
              e.sourceId as sourceId,
              e.deltaXp as deltaXp,
              e.createdAt as createdAt
            from XpEventEntity e
            where e.user.id = :userId
              and (
                    :beforeCreatedAt is null
                 or e.createdAt < :beforeCreatedAt
                 or (e.createdAt = :beforeCreatedAt and e.id < :beforeId)
              )
            order by e.createdAt desc, e.id desc
            """)
    List<XpEventRow> findHistoryForUser(
            @Param("userId") Long userId,
            @Param("beforeCreatedAt") LocalDateTime beforeCreatedAt,
            @Param("beforeId") Long beforeId,
            Pageable pageable);

    interface XpEventRow {
        Long getId();

        UUID getEventId();

        String getEventKey();

        String getSourceType();

        Long getSourceId();

        int getDeltaXp();

        LocalDateTime getCreatedAt();
    }
}
