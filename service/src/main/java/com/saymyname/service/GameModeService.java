package com.saymyname.service;

import com.saymyname.core.model.game.options.GameMode;
import com.saymyname.persistence.dao.GameModeDao;
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
