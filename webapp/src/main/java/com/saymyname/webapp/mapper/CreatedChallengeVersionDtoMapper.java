package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.challenge.ChallengeVersion;
import com.saymyname.webapp.dto.CreatedChallengeVersionDto;

@Component
public class CreatedChallengeVersionDtoMapper {

    private final ChallengeDtoMapper challengeDtoMapper;

    public CreatedChallengeVersionDtoMapper(ChallengeDtoMapper challengeDtoMapper) {
        this.challengeDtoMapper = challengeDtoMapper;
    }

    public CreatedChallengeVersionDto toDto(ChallengeVersion model) {
        return new CreatedChallengeVersionDto(
                model.getVersionNumber(),
                model.getStartDate(),
                model.getFirstSeason().getSeasonNumber(),
                challengeDtoMapper.toDto(model.getChallenge()),
                model.getQuestionCount()
            );
    }
    
}
