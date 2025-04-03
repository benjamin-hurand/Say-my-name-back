package com.saymyname.persistence.repository;

import com.saymyname.persistence.entity.PersonAttributeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonAttributeRepository extends JpaRepository<PersonAttributeEntity, Long> {

    @Query("SELECT pa FROM PersonAttributeEntity pa JOIN pa.attribute a " +
           "WHERE pa.person.id = (SELECT p.person.id FROM PhotoEntity p WHERE p.id = :photoId) " +
           "AND pa.validFrom <= CURRENT_TIMESTAMP " +
           "AND (pa.validTo IS NULL OR pa.validTo >= CURRENT_TIMESTAMP)")
    List<PersonAttributeEntity> findAttributesByPhotoId(@Param("photoId") Long photoId);

}
