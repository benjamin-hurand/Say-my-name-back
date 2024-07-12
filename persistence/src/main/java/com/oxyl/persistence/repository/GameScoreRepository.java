package com.oxyl.persistence.repository;

import com.oxyl.persistence.entity.GameScoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameScoreRepository extends JpaRepository<GameScoreEntity, Long> {
    
    List<GameScoreEntity> findByUser_Id(Long userId);
    
    List<GameScoreEntity> findByGameCategory_Id(Long gameCategoryId);
    
    List<GameScoreEntity> findByType(String type);
}
