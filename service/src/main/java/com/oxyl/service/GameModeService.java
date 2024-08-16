package com.oxyl.service;

import com.oxyl.core.model.game.options.GameMode;
import com.oxyl.persistence.dao.GameModeDao;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GameModeService {
    private GameModeDao gameModeDao;

    public GameModeService(GameModeDao gameModeDao) {
        this.gameModeDao = gameModeDao;
    }

    public List<GameMode> getAllGameModes() {
        return gameModeDao.findAll();
    }

}
