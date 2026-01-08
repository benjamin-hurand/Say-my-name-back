// src/main/java/com/saymyname/service/GameModeService.java
package com.saymyname.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.saymyname.core.model.quiz.options.GameMode;
import com.saymyname.persistence.dao.GameModeDao;

@Service
public class GameModeService {

    private final GameModeDao gameModeDao;

    public GameModeService(GameModeDao gameModeDao) {
        this.gameModeDao = Objects.requireNonNull(gameModeDao, "gameModeDao");
    }

    public List<GameMode> getAllGameModes() {
        return gameModeDao.findAll();
    }

    public GameMode findByIdOrThrow(Long id) {
        return gameModeDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("GameMode not found: id=" + id));
    }
}
