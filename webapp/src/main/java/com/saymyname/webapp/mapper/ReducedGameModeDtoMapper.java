package com.saymyname.webapp.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.game.options.GameMode;
import com.saymyname.core.model.game.options.GameModeAttribute;
import com.saymyname.webapp.dto.ReducedGameModeDto;

@Component
public class ReducedGameModeDtoMapper {
    
    public GameMode toModel(ReducedGameModeDto dto) {
        List<GameModeAttribute> gameModeAttributes = dto.attributeIds().stream()
            .map(attrId -> new GameModeAttribute.Builder().withAttribute(new Attribute.Builder().withId(attrId).build()).build())
            .toList();
        return new GameMode.Builder()
                .withId(dto.id())
                .withGameModeAttributes(gameModeAttributes)
                .withOperator(dto.operator())
                .build();
    }
    
}
