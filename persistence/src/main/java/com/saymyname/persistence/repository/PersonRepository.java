package com.saymyname.persistence.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saymyname.persistence.entity.organization.PersonEntity;

@Repository
public interface PersonRepository extends JpaRepository<PersonEntity, Long>, PersonRepositoryCustom {

  // JPQL → org auto
  // 1) p.attributes + pa.attribute (ManyToOne)
  @Query("""
      select distinct p
      from PersonEntity p
      left join fetch p.attributes pa
      left join fetch pa.attribute attr
      where p.id = :personId
      """)
  Optional<PersonEntity> fetchAttributesGraph(@Param("personId") Long personId);

  // JPQL → org auto
  // 2) p.photos
  @Query("""
      select distinct p
      from PersonEntity p
      left join fetch p.photos ph
      where p.id = :personId
      """)
  Optional<PersonEntity> fetchPhotos(@Param("personId") Long personId);
}
