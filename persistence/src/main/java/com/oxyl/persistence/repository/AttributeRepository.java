package com.oxyl.persistence.repository;

import com.oxyl.persistence.entity.AttributeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttributeRepository extends JpaRepository<AttributeEntity, Long> {
    
    AttributeEntity findByAttributeName(String attributeName);
}
