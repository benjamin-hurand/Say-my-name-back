package com.oxyl.persistence.repository;

import com.oxyl.persistence.entity.ThemeAttributesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ThemeAttributesRepository extends JpaRepository<ThemeAttributesEntity, Long> {
    
    List<ThemeAttributesEntity> findByTheme_Id(Long themeId);
    
    List<ThemeAttributesEntity> findByAttribute_Id(Long attributeId);
}
