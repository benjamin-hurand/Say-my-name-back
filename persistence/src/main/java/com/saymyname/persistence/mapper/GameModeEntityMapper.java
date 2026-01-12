package com.saymyname.persistence.mapper;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.quiz.options.GameMode;
import com.saymyname.persistence.entity.organization.GameModeAttributeEntity;
import com.saymyname.persistence.entity.organization.GameModeEntity;

@Component
public class GameModeEntityMapper {

    private final GameModeAttributeEntityMapper gameModeAttributeEntityMapper;

    public GameModeEntityMapper(GameModeAttributeEntityMapper gameModeAttributeEntityMapper) {
        this.gameModeAttributeEntityMapper = gameModeAttributeEntityMapper;
    }

    /**
     * Legacy full mapping Model -> Entity.
     * Attention: si tu utilises orphanRemoval/cascade et que tu veux "replace"
     * proprement, il vaut mieux faire la gestion de liste dans le DAO
     * (comme tu le fais avec applyReplaceAttributes()).
     */
    public GameModeEntity toEntity(GameMode gameMode) {
        if (gameMode == null)
            return null;

        GameModeEntity e = new GameModeEntity();
        // id: si présent, JPA le prendra comme un "merge" si attaché, sinon ignore en
        // persist
        // mais tu ne l'utilises normalement pas sur create.
        if (gameMode.getId() != null) {
            e.setId(gameMode.getId());
        }

        e.setGameModeTitle(gameMode.getTitle());
        e.setGameModeDescription(gameMode.getDescription());
        e.setOperator(gameMode.getOperator());

        List<GameModeAttributeEntity> attrs = (gameMode.getGameModeAttributes() == null)
                ? Collections.emptyList()
                : gameMode.getGameModeAttributes().stream()
                        .map(gameModeAttributeEntityMapper::toEntity)
                        .toList();

        // IMPORTANT: relier l'owner côté entity (mappedBy)
        // et remplir la collection existante
        e.getGameModeAttributes().clear();
        for (GameModeAttributeEntity a : attrs) {
            a.setGameMode(e);
            e.getGameModeAttributes().add(a);
        }

        return e;
    }

    public GameModeEntity toRefEntity(Long id) {
        if (id == null)
            return null;
        GameModeEntity e = new GameModeEntity();
        e.setId(id);
        return e;
    }

    /** Optionnel: si tu veux depuis un modèle short */
    public GameModeEntity toRefEntity(GameMode model) {
        return model == null ? null : toRefEntity(model.getId());
    }

    /**
     * Utilisé par ton DAO sur create.
     * On mappe uniquement les champs simples; la liste des attributes est gérée
     * dans le DAO.
     */
    public GameModeEntity toNewEntity(GameMode model) {
        if (model == null)
            return null;

        GameModeEntity e = new GameModeEntity();
        e.setGameModeTitle(model.getTitle());
        e.setGameModeDescription(model.getDescription());
        e.setOperator(model.getOperator());
        return e;
    }

    /**
     * Utilisé par ton DAO sur update.
     * Met à jour uniquement les champs simples; la liste est gérée dans le DAO.
     */
    public void updateEntity(GameModeEntity target, GameMode model) {
        if (target == null || model == null)
            return;

        target.setGameModeTitle(model.getTitle());
        target.setGameModeDescription(model.getDescription());
        target.setOperator(model.getOperator());
        // PAS de gestion de liste ici (DAO s'en charge)
    }

    public GameMode toModel(GameModeEntity gameModeEntity) {
        if (gameModeEntity == null)
            return null;

        return new GameMode.Builder()
                .withId(gameModeEntity.getId())
                .withVersion(gameModeEntity.getVersion()) // NEW
                .withTitle(gameModeEntity.getGameModeTitle())
                .withDescription(gameModeEntity.getGameModeDescription())
                .withGameModeAttributes(
                        gameModeEntity.getGameModeAttributes() == null
                                ? List.of()
                                : gameModeEntity.getGameModeAttributes().stream()
                                        .map(gameModeAttributeEntityMapper::toModel)
                                        .toList())
                .withOperator(gameModeEntity.getOperator())
                .build();
    }

    public GameMode toShortModel(GameModeEntity gameModeEntity) {
        if (gameModeEntity == null)
            return null;
        return new GameMode.Builder()
                .withId(gameModeEntity.getId())
                .withVersion(gameModeEntity.getVersion()) // NEW (optionnel)
                .build();
    }
}
