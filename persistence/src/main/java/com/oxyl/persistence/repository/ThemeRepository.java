package com.oxyl.persistence.repository;

import com.oxyl.persistence.entity.ThemeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ThemeRepository extends JpaRepository<ThemeEntity, Long> {
    
    ThemeEntity findByThemeTitle(String themeTitle);
}
