package com.oxyl.persistence.repository;

import com.oxyl.persistence.entity.PersonEntity;
import com.oxyl.persistence.entity.PhotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<PhotoEntity, Long> {
    
    List<PhotoEntity> findByPerson_Id(Long personId);
    
    List<PhotoEntity> findByPhotoUrl(String photoUrl);

    @Query("SELECT p.id FROM PhotoEntity p")
    List<Long> findAllPhotoIds();

    @Query("SELECT p.person FROM PhotoEntity p WHERE p.id = :photoId")
    Optional<PersonEntity> findPersonByPhotoId(@Param("photoId") Long photoId);
}
