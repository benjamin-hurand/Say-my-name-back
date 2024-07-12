package com.oxyl.persistence.repository;

import com.oxyl.persistence.entity.PhotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<PhotoEntity, Long> {
    
    List<PhotoEntity> findByPerson_Id(Long personId);
    
    List<PhotoEntity> findByPhotoUrl(String photoUrl);
}
