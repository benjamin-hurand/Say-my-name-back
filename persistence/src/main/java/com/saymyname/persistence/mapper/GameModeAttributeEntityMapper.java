package com.saymyname.persistence.mapper;

import com.saymyname.core.model.game.options.GameModeAttribute;
import com.saymyname.persistence.entity.GameModeAttributeEntity;
import org.springframework.stereotype.Component;

@Component
public class GameModeAttributeEntityMapper {

    private final AttributeEntityMapper attributeEntityMapper;

    public GameModeAttributeEntityMapper(AttributeEntityMapper attributeEntityMapper) {
        this.attributeEntityMapper = attributeEntityMapper;
    }

    public GameModeAttributeEntity toEntity(GameModeAttribute gameModeAttribute) {
        return new GameModeAttributeEntity(gameModeAttribute.getId(), attributeEntityMapper.toEntity(gameModeAttribute.getAttribute()));
    }

    public GameModeAttribute toModel(GameModeAttributeEntity gameModeAttributeEntity) {
        return new GameModeAttribute.Builder()
                .withId(gameModeAttributeEntity.getId())
                .withAttribute(attributeEntityMapper.toModel(gameModeAttributeEntity.getAttribute()))
                .build();
    }
}
