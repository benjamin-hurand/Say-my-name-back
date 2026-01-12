package com.saymyname.service;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.quiz.options.GameMode;
import com.saymyname.core.model.quiz.options.GameModeAttribute;
import com.saymyname.persistence.dao.GameModeDao;

@Service
public class GameModeService {

    private final GameModeDao gameModeDao;

    public GameModeService(GameModeDao gameModeDao) {
        this.gameModeDao = Objects.requireNonNull(gameModeDao, "gameModeDao");
    }

    @Transactional(readOnly = true)
    public List<GameMode> getAllGameModes() {
        return gameModeDao.findAll();
    }

    @Transactional(readOnly = true)
    public GameMode findByIdOrThrow(Long id) {
        return gameModeDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("GameMode not found: id=" + id));
    }

    @Transactional
    public GameMode create(GameMode toCreate) {
        validate(toCreate, false);
        return gameModeDao.create(toCreate);
    }

    @Transactional
    public GameMode update(GameMode toUpdate) {
        validate(toUpdate, true);
        return gameModeDao.update(toUpdate);
    }

    @Transactional
    public void delete(Long id) {
        if (id == null)
            throw new IllegalArgumentException("id is required");
        boolean deleted = gameModeDao.delete(id);
        if (!deleted)
            throw new java.util.NoSuchElementException("GameMode not found: id=" + id);
    }

    @Transactional
    public void replaceAttributes(Long gameModeId, List<GameModeAttribute> attributes) {
        if (gameModeId == null)
            throw new IllegalArgumentException("gameModeId is required");
        gameModeDao.replaceAttributes(gameModeId, attributes);
    }

    private static void validate(GameMode gm, boolean requireId) {
        if (gm == null)
            throw new IllegalArgumentException("GameMode is required");

        if (requireId && gm.getId() == null)
            throw new IllegalArgumentException("id is required");
        if (gm.getTitle() == null || gm.getTitle().isBlank())
            throw new IllegalArgumentException("title is required");

        String op = gm.getOperator();
        if (!"AND".equals(op) && !"OR".equals(op))
            throw new IllegalArgumentException("operator must be AND or OR");
    }
}
