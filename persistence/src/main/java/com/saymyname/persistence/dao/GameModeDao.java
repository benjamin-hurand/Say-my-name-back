// src/main/java/com/saymyname/persistence/dao/GameModeDao.java
package com.saymyname.persistence.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.quiz.options.GameMode;
import com.saymyname.persistence.mapper.GameModeEntityMapper;
import com.saymyname.persistence.repository.GameModeRepository;

@Repository
@Transactional
public class GameModeDao {

    private final GameModeRepository gameModeRepository;
    private final GameModeEntityMapper gameModeEntityMapper;

    public GameModeDao(GameModeRepository gameModeRepository, GameModeEntityMapper gameModeEntityMapper) {
        this.gameModeRepository = gameModeRepository;
        this.gameModeEntityMapper = gameModeEntityMapper;
    }

    public List<GameMode> findAll() {
        return gameModeRepository.findAll().stream()
                .map(gameModeEntityMapper::toModel)
                .toList();
    }

    public Optional<GameMode> findById(Long id) {
        if (id == null)
            return Optional.empty();
        return gameModeRepository.findById(id).map(gameModeEntityMapper::toModel);
    }

    // (optionnel) si tu l'utilises ailleurs
    public Optional<GameMode> findByGameModeTitle(String title) {
        if (title == null || title.isBlank())
            return Optional.empty();
        var entity = gameModeRepository.findByGameModeTitle(title);
        return Optional.ofNullable(entity).map(gameModeEntityMapper::toModel);
    }
}
