package com.oxyl.persistence.repository;

import com.oxyl.persistence.entity.GameModeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameModeRepository extends JpaRepository<GameModeEntity, Long> {
    
    GameModeEntity findByGameModeTitle(String gameModeTitle);
}
