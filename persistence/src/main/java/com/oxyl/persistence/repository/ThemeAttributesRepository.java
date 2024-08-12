package com.oxyl.persistence.repository;

import com.oxyl.persistence.entity.ThemeAttributeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThemeAttributesRepository extends JpaRepository<ThemeAttributeEntity, Long> {
    
    List<ThemeAttributeEntity> findByTheme_Id(Long themeId);
    
    List<ThemeAttributeEntity> findByAttribute_Id(Long attributeId);
}
