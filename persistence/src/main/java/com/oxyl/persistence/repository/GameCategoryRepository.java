package com.oxyl.persistence.repository;

import com.oxyl.persistence.entity.GameCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameCategoryRepository extends JpaRepository<GameCategoryEntity, Long> {
    
    List<GameCategoryEntity> findByGameMode_Id(Long gameModeId);
}
