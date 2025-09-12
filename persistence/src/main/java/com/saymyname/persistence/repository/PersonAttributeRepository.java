package com.saymyname.persistence.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saymyname.persistence.entity.PersonAttributeEntity;

@Repository
public interface PersonAttributeRepository extends JpaRepository<PersonAttributeEntity, Long> {

  @Query(value = "SELECT COUNT(DISTINCT pa.person_id) " +
      "FROM persons_attributes pa " +
      "WHERE pa.value >= ?1 " +
      "  AND pa.value < ?2 " +
      "  AND pa.valid_from <= ?3 " +
      "  AND (pa.valid_to IS NULL OR pa.valid_to >= ?3) " +
      "  AND pa.attribute_id = ?4", nativeQuery = true)
  long countPersonsMatchingFilter(String minValue, String nextValue, LocalDateTime validFor, Long attributeId);

  // Actifs “runtime” : exclude pending_delete — ordre déterministe
  @Query("""
      SELECT pa FROM PersonAttributeEntity pa JOIN pa.attribute a
      WHERE pa.person.id = :personId
        AND pa.pendingDelete = false
        AND pa.validFrom <= CURRENT_TIMESTAMP
        AND (pa.validTo IS NULL OR pa.validTo > CURRENT_TIMESTAMP)
      ORDER BY pa.validFrom ASC, pa.id ASC
      """)
  List<PersonAttributeEntity> findAttributesByPersonIdActive(@Param("personId") Long personId);

  // Actifs par attribut, hors pending_delete — ordre déterministe
  @Query("""
      SELECT pa FROM PersonAttributeEntity pa
      WHERE pa.person.id = :personId
        AND pa.attribute.id = :attributeId
        AND pa.pendingDelete = false
        AND pa.validFrom <= CURRENT_TIMESTAMP
        AND (pa.validTo IS NULL OR pa.validTo > CURRENT_TIMESTAMP)
      ORDER BY pa.validFrom ASC, pa.id ASC
      """)
  List<PersonAttributeEntity> findActiveByPersonAndAttributeExcludingPending(
      @Param("personId") Long personId,
      @Param("attributeId") Long attributeId);

  // NON-pending à partir de NOW (actives + futures) — ordre déterministe
  @Query("""
      SELECT pa FROM PersonAttributeEntity pa
      WHERE pa.person.id = :personId
        AND pa.attribute.id = :attributeId
        AND pa.pendingDelete = false
        AND (pa.validTo IS NULL OR pa.validTo > :now)
      ORDER BY pa.validFrom ASC, pa.id ASC
      """)
  List<PersonAttributeEntity> findNonPendingFromNowByPersonAndAttribute(
      @Param("personId") Long personId,
      @Param("attributeId") Long attributeId,
      @Param("now") LocalDateTime now);

  /**
   * SOFT-CLOSE en lot : set pending_delete=true, valid_to = :seasonEnd (seulement
   * sur les lignes actives au :now)
   */
  @Modifying
  @Query("""
      UPDATE PersonAttributeEntity pa
         SET pa.pendingDelete = true,
             pa.validTo = :seasonEnd
       WHERE pa.person.id = :personId
         AND pa.id IN (:ids)
         AND pa.pendingDelete = false
         AND pa.validFrom <= :now
         AND (pa.validTo IS NULL OR pa.validTo > :now)
      """)
  int softCloseAllByIdsAndPersonId(@Param("personId") Long personId,
      @Param("ids") List<Long> ids,
      @Param("seasonEnd") LocalDateTime seasonEnd,
      @Param("now") LocalDateTime now);

  /**
   * Hard delete des lignes FUTURES non-pending (valid_from > :now)
   */
  @Modifying
  @Query("""
      DELETE FROM PersonAttributeEntity pa
       WHERE pa.person.id = :personId
         AND pa.id IN (:ids)
         AND pa.pendingDelete = false
         AND pa.validFrom > :now
      """)
  int hardDeleteFutureByIdsAndPersonId(@Param("personId") Long personId,
      @Param("ids") List<Long> ids,
      @Param("now") LocalDateTime now);

  /**
   * Hard delete en une requête : toutes les lignes marquées is_pending_delete=1
   * et expirées
   */
  @Modifying
  @Query(value = """
      DELETE FROM persons_attributes
       WHERE is_pending_delete = 1
         AND valid_to IS NOT NULL
         AND valid_to < :cutoff
      """, nativeQuery = true)
  int hardDeleteExpiredPendingAttributes(@Param("cutoff") LocalDateTime cutoff);
}
