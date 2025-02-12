package com.saymyname.persistence.repository;

import com.saymyname.persistence.entity.AttributeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttributeRepository extends JpaRepository<AttributeEntity, Long> {
    
    AttributeEntity findByAttributeName(String attributeName);

    List<AttributeEntity> findByFilterTrue();

    List<AttributeEntity> findBySortTrue();
}
