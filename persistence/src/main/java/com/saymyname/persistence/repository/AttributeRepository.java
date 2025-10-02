package com.saymyname.persistence.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saymyname.persistence.entity.organization.attribute.AttributeEntity;

@Repository
public interface AttributeRepository extends JpaRepository<AttributeEntity, Long> {

    AttributeEntity findByAttributeName(String attributeName);

    List<AttributeEntity> findByFilterTrue();

    List<AttributeEntity> findBySortTrue();
}
