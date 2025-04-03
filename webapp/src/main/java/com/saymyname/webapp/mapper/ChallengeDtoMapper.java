package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.challenge.Challenge;
import com.saymyname.core.model.common.User;
import com.saymyname.core.model.game.options.GameMode;
import com.saymyname.webapp.dto.AddChallengeDto;
import com.saymyname.webapp.dto.ChallengeDto;

@Component
public class ChallengeDtoMapper {
    private final GameModeDtoMapper gameModeDtoMapper;
    private final GameAttributeFilterDtoMapper gameAttributeFilterDtoMapper;
    private final UserDtoMapper userDtoMapper;
    private final ReducedGameAttributeFilterDtoMapper reducedGameAttributeFilterDtoMapper;

    public ChallengeDtoMapper(
            GameModeDtoMapper gameModeDtoMapper,
            GameAttributeFilterDtoMapper gameAttributeFilterDtoMapper,
            UserDtoMapper userDtoMapper,
            ReducedGameAttributeFilterDtoMapper reducedGameAttributeFilterDtoMapper
        ) {
        this.gameModeDtoMapper = gameModeDtoMapper;
        this.gameAttributeFilterDtoMapper = gameAttributeFilterDtoMapper;
        this.userDtoMapper = userDtoMapper;
        this.reducedGameAttributeFilterDtoMapper = reducedGameAttributeFilterDtoMapper;
    }

    public ChallengeDto toDto(Challenge model) {
        return new ChallengeDto(
                    model.getId(),
                    model.getDescription(),
                    gameModeDtoMapper.toDto(model.getGameMode()),
                    gameAttributeFilterDtoMapper.toDto(model.getFilterAttribute()),
                    model.getCreationDate(),
                    userDtoMapper.toDto(model.getCreator())
                );
    }

    public Challenge toModel(ChallengeDto dto) {
        return new Challenge.Builder()
                .withId(dto.id())
                .withDescription(dto.description())
                .withGameMode(gameModeDtoMapper.toModel(dto.gameMode()))
                .withFilterAttribute(gameAttributeFilterDtoMapper.toModel(dto.attributeFilter()))
                .withCreationDate(dto.creationDate())
                .withCreator(userDtoMapper.toModel(dto.creator()))
                .build();
    }

    public Challenge toModel(AddChallengeDto dto) {
        return new Challenge.Builder()
                .withDescription(dto.description())
                .withGameMode(new GameMode.Builder().withId(dto.gameModeId()).build())
                .withFilterAttribute(reducedGameAttributeFilterDtoMapper.toModel(dto.attributeFilter()))
                .withCreator(new User.Builder().withId(dto.creatorId()).build())
                .build();
    }
}
