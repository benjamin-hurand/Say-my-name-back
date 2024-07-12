package com.oxyl.persistence.repository;

import com.oxyl.persistence.entity.PersonAttributeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PersonAttributeRepository extends JpaRepository<PersonAttributeEntity, Long> {
    
    List<PersonAttributeEntity> findByAttribute_Id(Long attributeId);
    
    List<PersonAttributeEntity> findByPerson_Id(Long personId);
    
    List<PersonAttributeEntity> findByValue(String value);
}
