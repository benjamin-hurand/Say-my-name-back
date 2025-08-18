package com.saymyname.persistence.repository;

import com.saymyname.persistence.entity.PersonAttributeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PersonAttributeRepository extends JpaRepository<PersonAttributeEntity, Long> {

       @Query("SELECT pa FROM PersonAttributeEntity pa JOIN pa.attribute a " +
                     "WHERE pa.person.id = (SELECT p.person.id FROM PhotoEntity p WHERE p.id = :photoId) " +
                     "AND pa.validFrom <= CURRENT_TIMESTAMP " +
                     "AND (pa.validTo IS NULL OR pa.validTo >= CURRENT_TIMESTAMP)")
       List<PersonAttributeEntity> findAttributesByPhotoId(@Param("photoId") Long photoId);

       @Query(value = "SELECT COUNT(DISTINCT pa.person_id) " +
                     "FROM persons_attributes pa " +
                     "WHERE pa.value >= ?1 " +
                     "  AND pa.value < ?2 " +
                     "  AND pa.valid_from <= ?3 " +
                     "  AND (pa.valid_to IS NULL OR pa.valid_to >= ?3) " +
                     "  AND pa.attribute_id = ?4", nativeQuery = true)
       long countPersonsMatchingFilter(String minValue, String nextValue, LocalDateTime validFor, Long attributeId);

       @Query("SELECT pa FROM PersonAttributeEntity pa JOIN pa.attribute a " +
                     "WHERE pa.person.id = :personId " +
                     "AND pa.validFrom <= CURRENT_TIMESTAMP " +
                     "AND (pa.validTo IS NULL OR pa.validTo >= CURRENT_TIMESTAMP)")
       List<PersonAttributeEntity> findAttributesByPersonId(@Param("personId") Long personId);

       @Query("SELECT COUNT(pa) FROM PersonAttributeEntity pa " +
                     "WHERE pa.person.id = :personId AND pa.attribute.id = :attributeId " +
                     "AND pa.validFrom <= CURRENT_TIMESTAMP " +
                     "AND (pa.validTo IS NULL OR pa.validTo >= CURRENT_TIMESTAMP)")
       long countActiveByPersonAndAttribute(@Param("personId") Long personId,
                     @Param("attributeId") Long attributeId);

       @Query("SELECT CASE WHEN COUNT(pa) > 0 THEN true ELSE false END " +
                     "FROM PersonAttributeEntity pa " +
                     "WHERE pa.person.id = :personId AND pa.attribute.id = :attributeId " +
                     "AND pa.value = :value " +
                     "AND pa.validFrom <= CURRENT_TIMESTAMP " +
                     "AND (pa.validTo IS NULL OR pa.validTo >= CURRENT_TIMESTAMP)")
       boolean existsActiveDuplicateValue(@Param("personId") Long personId,
                     @Param("attributeId") Long attributeId,
                     @Param("value") String value);

       long countDistinctByAttribute_IdAndValueBetween(long attributeId, String min, String max);

       @Modifying
       @Query(value = """
                     DELETE pa
                     FROM persons_attributes pa
                     JOIN attributes a ON a.id = pa.attribute_id
                     LEFT JOIN (
                     SELECT person_id, attribute_id, COUNT(*) AS cnt
                     FROM persons_attributes
                     GROUP BY person_id, attribute_id
                     ) c ON c.person_id = pa.person_id AND c.attribute_id = pa.attribute_id
                     WHERE pa.id = :id
                     AND pa.person_id = :personId
                     AND (
                            a.required = 0
                            OR (a.required = 1 AND a.`unique` = 0 AND c.cnt > 1)
                     )
                     """, nativeQuery = true)
       int safeDeleteByIdAndPersonId(@Param("id") Long id, @Param("personId") Long personId);

       @Modifying
       @Query("""
                     UPDATE PersonAttributeEntity pa
                     SET pa.value = :value
                     WHERE pa.id = :id
                     AND pa.person.id = :personId
                     """)
       int updateValue(@Param("id") Long id,
                     @Param("personId") Long personId,
                     @Param("value") String value);
}
