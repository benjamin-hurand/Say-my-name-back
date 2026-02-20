package com.saymyname.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.quiz.options.GameMode;
import com.saymyname.core.model.quiz.options.GameModeAttribute;
import com.saymyname.persistence.dao.AttributeDao;

@Service
public class GameModeService {

    private final AttributeDao attributeDao;

    public GameModeService(AttributeDao attributeDao) {
        this.attributeDao = Objects.requireNonNull(attributeDao, "attributeDao");
    }

    @Transactional(readOnly = true)
    public List<GameMode> getAllGameModes() {
        return attributeDao.findAll().stream()
                .map(this::toSingleAttributeMode)
                .toList();
    }

    @Transactional(readOnly = true)
    public GameMode findByIdOrThrow(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("gameModeId is required");
        }
        Attribute attribute = attributeDao.findById(id)
                .orElseThrow(() -> new NoSuchElementException("GameMode not found: id=" + id));
        return toSingleAttributeMode(attribute);
    }

    @Transactional
    public GameMode create(GameMode model) {
        return normalizeCompatibilityModel(model, null);
    }

    @Transactional
    public GameMode update(GameMode model) {
        if (model == null || model.getId() == null) {
            throw new IllegalArgumentException("GameMode id is required for update");
        }
        return normalizeCompatibilityModel(model, model.getId());
    }

    @Transactional
    public void delete(Long id) {
        // No dedicated persistence for GameMode in current model.
    }

    @Transactional
    public void replaceAttributes(Long id, List<GameModeAttribute> attributes) {
        // No dedicated persistence for GameMode in current model.
    }

    private GameMode toSingleAttributeMode(Attribute attribute) {
        Long id = attribute.getId();
        GameModeAttribute gma = GameModeAttribute.builder()
                .id(id)
                .attribute(attribute)
                .build();

        return GameMode.builder()
                .id(id)
                .title(attribute.getName())
                .description(null)
                .operator("AND")
                .gameModeAttributes(List.of(gma))
                .attributes(List.of(attribute))
                .build();
    }

    private GameMode normalizeCompatibilityModel(GameMode model, Long forcedId) {
        if (model == null) {
            throw new IllegalArgumentException("GameMode payload is required");
        }

        Long resolvedId = forcedId != null ? forcedId : model.getId();
        if (resolvedId == null && model.getGameModeAttributes() != null && !model.getGameModeAttributes().isEmpty()) {
            resolvedId = model.getGameModeAttributes().stream()
                    .map(GameModeAttribute::getAttributeId)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null);
        }

        List<GameModeAttribute> attrs = model.getGameModeAttributes() == null
                ? List.of()
                : model.getGameModeAttributes().stream()
                        .filter(Objects::nonNull)
                        .toList();

        String operator = model.getOperator();
        if (operator == null || operator.isBlank()) {
            operator = "AND";
        }

        return GameMode.builder()
                .id(resolvedId)
                .title(model.getTitle())
                .description(model.getDescription())
                .operator(operator)
                .gameModeAttributes(attrs)
                .attributes(model.getAttributes() == null ? List.of() : model.getAttributes())
                .build();
    }
}
