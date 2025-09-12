// src/main/java/com/saymyname/persistence/repository/ChangeRequestRepository.java
package com.saymyname.persistence.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import com.saymyname.core.model.enums.ChangeStatus;
import com.saymyname.persistence.entity.ChangeRequestEntity;

public interface ChangeRequestRepository extends JpaRepository<ChangeRequestEntity, Long> {

        @Query("""
                        select cr
                        from ChangeRequestEntity cr
                        join fetch cr.person p
                        join fetch cr.requester r
                        join fetch cr.attribute a
                        left join fetch cr.items i
                        left join fetch i.personAttribute pa
                        where cr.id = :id
                        """)
        Optional<ChangeRequestEntity> findByIdDeep(@Param("id") Long id);

        @Query("""
                        select cr
                        from ChangeRequestEntity cr
                        join cr.person p
                        join cr.requester r
                        join cr.attribute a
                        where p.id = :personId and r.id = :requesterId and a.id = :attributeId
                          and cr.status in :openStatuses
                        order by cr.createdAt desc
                        """)
        Optional<ChangeRequestEntity> findFirstOpenByTriplet(
                        @Param("personId") Long personId,
                        @Param("requesterId") Long requesterId,
                        @Param("attributeId") Long attributeId,
                        @Param("openStatuses") Collection<ChangeStatus> openStatuses);

        @Query("""
                        select distinct cr
                        from ChangeRequestEntity cr
                        join fetch cr.requester r
                        join fetch cr.person p
                        join fetch cr.attribute a
                        left join fetch cr.resolvedBy rb
                        left join fetch cr.items i
                        left join fetch i.personAttribute pa
                        where r.id = :userId and cr.status in :statuses
                        order by cr.createdAt desc
                        """)
        List<ChangeRequestEntity> findByUserIdAndStatusesDeep(
                        @Param("userId") Long userId,
                        @Param("statuses") Collection<ChangeStatus> statuses);
}
