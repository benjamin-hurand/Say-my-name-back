package com.oxyl.persistence.dao;

import com.oxyl.core.model.game.options.GameMode;
import com.oxyl.persistence.mapper.GameModeEntityMapper;
import com.oxyl.persistence.repository.GameModeRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class GameModeDao {
    private GameModeRepository gameModeRepository;
    private GameModeEntityMapper gameModeEntityMapper;

    public GameModeDao(GameModeRepository gameModeRepository, GameModeEntityMapper gameModeEntityMapper) {
        this.gameModeRepository = gameModeRepository;
        this.gameModeEntityMapper = gameModeEntityMapper;
    }

    public List<GameMode> findAll() {
        return gameModeRepository.findAll().stream().map(gameModeEntityMapper::toGameModel).toList();
    }
}
