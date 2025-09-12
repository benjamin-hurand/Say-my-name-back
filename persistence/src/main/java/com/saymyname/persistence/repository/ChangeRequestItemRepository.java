// src/main/java/com/saymyname/persistence/repository/ChangeRequestItemRepository.java
package com.saymyname.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

import com.saymyname.persistence.entity.ChangeRequestItemEntity;

@Repository
public interface ChangeRequestItemRepository extends JpaRepository<ChangeRequestItemEntity, Long> {

        /** Tous les items d’une enveloppe (pour clean/replace). */
        List<ChangeRequestItemEntity> findAllByChangeRequest_Id(Long changeRequestId);

        /** Supprime tous les items d’une enveloppe. */
        void deleteByChangeRequest_Id(Long changeRequestId);
}
