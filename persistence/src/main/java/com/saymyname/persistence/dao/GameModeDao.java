package com.saymyname.persistence.dao;

import com.saymyname.core.model.game.options.GameMode;
import com.saymyname.persistence.mapper.GameModeEntityMapper;
import com.saymyname.persistence.repository.GameModeRepository;
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
        return gameModeRepository.findAll().stream().map(gameModeEntityMapper::toModel).toList();
    }
}
