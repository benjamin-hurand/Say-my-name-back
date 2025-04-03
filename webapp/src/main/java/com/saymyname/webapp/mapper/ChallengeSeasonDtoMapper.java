package com.saymyname.webapp.mapper;

import com.saymyname.core.model.challenge.ChallengeSeason;
import com.saymyname.webapp.dto.ChallengeSeasonDto;
import org.springframework.stereotype.Component;

@Component
public class ChallengeSeasonDtoMapper {

    public ChallengeSeasonDto toDto(ChallengeSeason model) {
        if (model == null) {
            return null;
        }
        return new ChallengeSeasonDto(
            model.getId(),
            model.getSeasonNumber(),
            model.getStartDate(),
            model.getEndDate()
        );
    }
}
