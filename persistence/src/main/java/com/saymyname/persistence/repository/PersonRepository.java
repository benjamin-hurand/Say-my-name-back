// src/main/java/com/saymyname/persistence/repository/PersonRepository.java
package com.saymyname.persistence.repository;

import com.saymyname.persistence.entity.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<PersonEntity, Long>, PersonRepositoryCustom {

    List<PersonEntity> findByUserIsNull();

    // ID only (léger, pas d’ENTITY côté service)
    @Query("""
            select p.id
            from PersonEntity p
            where p.user.id = :userId
            """)
    Optional<Long> findIdByUserId(@Param("userId") Long userId);

    // 1) p.attributes + pa.attribute (ManyToOne)
    @Query("""
            select distinct p
            from PersonEntity p
            left join fetch p.attributes pa
            left join fetch pa.attribute attr
            where p.id = :personId
            """)
    Optional<PersonEntity> fetchAttributesGraph(@Param("personId") Long personId);

    // 2) p.photos
    @Query("""
            select distinct p
            from PersonEntity p
            left join fetch p.photos ph
            where p.id = :personId
            """)
    Optional<PersonEntity> fetchPhotos(@Param("personId") Long personId);
}
