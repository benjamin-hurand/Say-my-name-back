// src/main/java/com/saymyname/persistence/repository/FactRepository.java
package com.saymyname.persistence.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saymyname.persistence.entity.organization.FactEntity;

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

  // MIN/MAX pour attributs datetime (format ISO 'YYYY-MM-DDTHH:mm:ss' dans value)
  @Query(value = """
      SELECT f.attribute_id AS attributeId,
             REPLACE(DATE_FORMAT(MIN(STR_TO_DATE(REPLACE(f.value, 'T', ' '), '%Y-%m-%d %H:%i:%s')), '%Y-%m-%d %H:%i:%s'), ' ', 'T') AS minVal,
             REPLACE(DATE_FORMAT(MAX(STR_TO_DATE(REPLACE(f.value, 'T', ' '), '%Y-%m-%d %H:%i:%s')), '%Y-%m-%d %H:%i:%s'), ' ', 'T') AS maxVal
        FROM facts f
       WHERE f.attribute_id IN (:attributeIds)
         AND f.tenant_id = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
         AND f.is_deleted = 0
       GROUP BY f.attribute_id
      """, nativeQuery = true)
  List<Object[]> findDatetimeMinMaxByAttributeIds(@Param("attributeIds") Collection<Long> attributeIds);

  // Impact de suppression : nb de facts + nb de personnes distinctes par attribut
  @Query(value = """
      SELECT f.attribute_id AS attributeId,
             COUNT(*) AS factCount,
             COUNT(DISTINCT f.person_id) AS personCount
        FROM facts f
       WHERE f.attribute_id IN (:attributeIds)
         AND f.tenant_id = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
         AND f.is_deleted = 0
       GROUP BY f.attribute_id
      """, nativeQuery = true)
  List<Object[]> countFactsAndPersonsByAttributeIds(@Param("attributeIds") Collection<Long> attributeIds);

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
        AND f.tenantId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
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
        AND f.tenantId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
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
         AND f.tenantId = :#{T(com.saymyname.core.multitenancy.TenantContext).get()}
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

}
