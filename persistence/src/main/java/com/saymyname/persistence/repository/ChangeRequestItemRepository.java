package com.saymyname.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saymyname.persistence.entity.organization.ChangeRequestItemEntity;

@Repository
public interface ChangeRequestItemRepository extends JpaRepository<ChangeRequestItemEntity, Long> {

        /** Tous les items d’une enveloppe (pour clean/replace). */
        List<ChangeRequestItemEntity> findAllByChangeRequest_Id(Long changeRequestId);

        /**
         * Supprime tous les items d’une enveloppe.
         * ⚠ Bulk JPQL : on ajoute le garde-fou tenant explicite.
         * (Signature inchangée)
         */
        @Modifying
        @Query("""
                            DELETE FROM ChangeRequestItemEntity i
                             WHERE i.changeRequest.id = :changeRequestId
                               AND i.organizationId  = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
                        """)
        void deleteByChangeRequest_Id(@Param("changeRequestId") Long changeRequestId);
}
