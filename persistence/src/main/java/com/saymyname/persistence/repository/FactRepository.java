// src/main/java/com/saymyname/persistence/repository/FactRepository.java
package com.saymyname.persistence.repository;

import static org.hibernate.jpa.HibernateHints.HINT_FETCH_SIZE;
import static org.hibernate.jpa.HibernateHints.HINT_READ_ONLY;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saymyname.persistence.entity.organization.FactEntity;
import com.saymyname.persistence.projection.PersonAttrValueProjection;

import jakarta.persistence.QueryHint;

@Repository
public interface FactRepository extends JpaRepository<FactEntity, Long> {

  // MIN/MAX pour attributs numériques (valeurs stockées en texte)
  @Query(value = """
      SELECT f.attribute_id AS attributeId,
             MIN(CAST(f.value AS DECIMAL(20,6))) AS minVal,
             MAX(CAST(f.value AS DECIMAL(20,6))) AS maxVal
        FROM facts f
       WHERE f.attribute_id IN (:attributeIds)
         AND f.tenant_id = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
         AND f.is_deleted = 0
       GROUP BY f.attribute_id
      """, nativeQuery = true)
  List<Object[]> findNumberMinMaxByAttributeIds(@Param("attributeIds") Collection<Long> attributeIds);

  // MIN/MAX pour attributs date (format 'YYYY-MM-DD' dans value)
  @Query(value = """
      SELECT f.attribute_id AS attributeId,
             DATE_FORMAT(MIN(STR_TO_DATE(f.value, '%Y-%m-%d')), '%Y-%m-%d') AS minVal,
             DATE_FORMAT(MAX(STR_TO_DATE(f.value, '%Y-%m-%d')), '%Y-%m-%d') AS maxVal
        FROM facts f
       WHERE f.attribute_id IN (:attributeIds)
         AND f.tenant_id = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
         AND f.is_deleted = 0
       GROUP BY f.attribute_id
      """, nativeQuery = true)
  List<Object[]> findDateMinMaxByAttributeIds(@Param("attributeIds") Collection<Long> attributeIds);

  // Comptage par intervalle + validité
  @Query(value = """
      SELECT COUNT(DISTINCT f.person_id)
        FROM facts f
       WHERE f.value >= ?1
         AND f.value <  ?2
         AND f.valid_from <= ?3
         AND (f.valid_to IS NULL OR f.valid_to > ?3)
         AND f.attribute_id = ?4
         AND f.tenant_id = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
         AND f.is_deleted = 0
      """, nativeQuery = true)
  long countPersonsMatchingFilter(String minValue, String nextValue, LocalDateTime validFor, Long attributeId);

  // Actifs “runtime” (is_deleted=false) — ordre déterministe
  @Query("""
      SELECT f FROM FactEntity f
      WHERE f.personId = :personId
        AND f.deleted = false
        AND f.validFrom <= CURRENT_TIMESTAMP
        AND (f.validTo IS NULL OR f.validTo > CURRENT_TIMESTAMP)
      ORDER BY f.validFrom ASC, f.id ASC
      """)
  List<FactEntity> findAttributesByPersonIdActive(@Param("personId") Long personId);

  // Actifs à un instant donné
  @Query("""
      SELECT f FROM FactEntity f
      WHERE f.personId = :personId
        AND f.attributeId = :attributeId
        AND f.deleted = false
        AND f.validFrom <= :now
        AND (f.validTo IS NULL OR f.validTo > :now)
      ORDER BY f.validFrom ASC, f.id ASC
      """)
  List<FactEntity> findActiveAtByPersonAndAttributeExcludingDeleted(
      @Param("personId") Long personId,
      @Param("attributeId") Long attributeId,
      @Param("now") LocalDateTime now);

  // SOFT-CLOSE : is_deleted=true, valid_to=:now (uniquement sur actifs)
  @Modifying
  @Query("""
      UPDATE FactEntity f
         SET f.deleted = true,
             f.validTo = :now
       WHERE f.personId = :personId
         AND f.id IN (:ids)
         AND f.deleted = false
         AND f.validFrom <= :now
         AND (f.validTo IS NULL OR f.validTo > :now)
      """)
  int softCloseActiveByIdsAndPersonId(
      @Param("personId") Long personId,
      @Param("ids") List<Long> ids,
      @Param("now") LocalDateTime now);

  // Hard delete : toutes les lignes deleted=true et expirées
  @Modifying
  @Query(value = """
      DELETE FROM facts
       WHERE is_deleted = 1
         AND valid_to IS NOT NULL
         AND valid_to < :cutoff
         AND tenant_id = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
      """, nativeQuery = true)
  int hardDeleteExpiredDeletedFacts(@Param("cutoff") LocalDateTime cutoff);

  // Valeurs d'attributs primaires pour un batch de personnes
  @QueryHints({
      @QueryHint(name = HINT_READ_ONLY, value = "true"),
      @QueryHint(name = HINT_FETCH_SIZE, value = "500")
  })
  @Query("""
      select
        f.personId as personId,
        f.attributeId as attributeId,
        f.value as value,
        a.displayOrder as displayOrder
      from FactEntity f
        join f.attribute a
      where f.personId in :personIds
        and a.identitySource = true
        and f.deleted = false
        and f.validFrom <= CURRENT_TIMESTAMP
        and (f.validTo IS NULL OR f.validTo > CURRENT_TIMESTAMP)
      order by f.personId asc, a.displayOrder asc, f.id asc
      """)
  List<PersonAttrValueProjection> findPrimaryAttributesForPersons(
      @Param("personIds") Collection<Long> personIds);
}