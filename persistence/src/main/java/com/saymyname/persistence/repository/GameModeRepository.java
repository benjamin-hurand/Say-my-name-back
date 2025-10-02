package com.saymyname.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saymyname.persistence.entity.organization.GameModeEntity;

@Repository
public interface GameModeRepository extends JpaRepository<GameModeEntity, Long> {

    GameModeEntity findByGameModeTitle(String gameModeTitle);
}
