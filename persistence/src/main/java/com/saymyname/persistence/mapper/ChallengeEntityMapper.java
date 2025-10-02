package com.saymyname.persistence.mapper;

import org.springframework.stereotype.Component;
import com.saymyname.core.model.challenge.Challenge;
import com.saymyname.core.model.game.options.GameAttributeFilter;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.persistence.entity.organization.ChallengeEntity;

@Component
public class ChallengeEntityMapper {

    private final GameModeEntityMapper gameModeEntityMapper;
    private final AttributeEntityMapper attributeEntityMapper;
    private final UserEntityMapper userEntityMapper;

    public ChallengeEntityMapper(GameModeEntityMapper gameModeEntityMapper,
            AttributeEntityMapper attributeEntityMapper,
            UserEntityMapper userEntityMapper) {
        this.gameModeEntityMapper = gameModeEntityMapper;
        this.attributeEntityMapper = attributeEntityMapper;
        this.userEntityMapper = userEntityMapper;
    }

    public ChallengeEntity toEntity(Challenge model) {
        if (model == null) {
            return null;
        }
        ChallengeEntity entity = new ChallengeEntity();
        entity.setId(model.getId());
        entity.setDescription(model.getDescription());
        entity.setGameMode(gameModeEntityMapper.toEntity(model.getGameMode()));
        entity.setFilterAttribute(attributeEntityMapper.toEntity(model.getFilterAttribute().getAttribute()));
        entity.setMinFilterValue(model.getFilterAttribute().getMinValue());
        entity.setMaxFilterValue(model.getFilterAttribute().getMaxValue());
        entity.setCreationDate(model.getCreationDate());
        entity.setCreator(userEntityMapper.toEntity(model.getCreator()));
        return entity;
    }

    public Challenge toModel(ChallengeEntity entity) {
        if (entity == null) {
            return null;
        }
        // FILTER ATTRIBUTE
        Attribute attribute = attributeEntityMapper.toModel(entity.getFilterAttribute());
        String minValue = entity.getMinFilterValue();
        String maxValue = entity.getMaxFilterValue();
        GameAttributeFilter filterAttribute = new GameAttributeFilter.Builder()
                .withAttribute(attribute)
                .withMinValue(minValue)
                .withMaxValue(maxValue)
                .build();
        return new Challenge.Builder()
                .withId(entity.getId())
                .withDescription(entity.getDescription())
                .withGameMode(gameModeEntityMapper.toModel(entity.getGameMode()))
                .withFilterAttribute(filterAttribute)
                .withCreationDate(entity.getCreationDate())
                .withCreator(userEntityMapper.toShortModel(entity.getCreator()))
                .build();
    }
}
