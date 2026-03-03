package com.saymyname.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.enums.PhotoStatus;
import com.saymyname.persistence.entity.organization.PhotoEntity;

@Repository
public interface PhotoRepository extends JpaRepository<PhotoEntity, Long> {

  /**
   * Supprime la/les photos ayant un statut donné pour une personne.
   * ⚠ Bulk JPQL → ajout du garde-fou tenant explicite.
   */
  @Modifying
  @Transactional
  @Query(value = """
      DELETE FROM photos
       WHERE person_id       = :personId
         AND status          = :status
         AND tenant_id = :#{T(com.saymyname.core.multitenancy.OrgContext).get()}
      """, nativeQuery = true)
  long deleteByPersonIdAndStatus(Long personId, PhotoStatus status);

  // 🔎 Toutes les keys APPROVED (SELECT → filtre tenant auto)
  @Query("select p.storageKey from PhotoEntity p where p.status = com.saymyname.core.model.enums.PhotoStatus.APPROVED")
  List<String> findAllApprovedKeys();

  // (optionnel) toutes les keys (SELECT → filtre tenant auto)
  @Query("select p.storageKey from PhotoEntity p")
  List<String> findAllKeys();
}
