package com.saymyname.persistence.repository;

import com.saymyname.persistence.entity.PersonAttributeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
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
       long countPersonsMatchingFilter(String minValue, String nextValue, LocalDateTime seasonStart, Long attributeId);

       @Query("SELECT pa FROM PersonAttributeEntity pa JOIN pa.attribute a " +
                     "WHERE pa.person.id = :personId " +
                     "AND pa.validFrom <= CURRENT_TIMESTAMP " +
                     "AND (pa.validTo IS NULL OR pa.validTo >= CURRENT_TIMESTAMP)")
       List<PersonAttributeEntity> findAttributesByPersonId(@Param("personId") Long personId);

}
