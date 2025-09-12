package com.saymyname.webapp.mapper.challenge;

import com.saymyname.core.model.challenge.ChallengeSeason;
import com.saymyname.webapp.dto.challenge.ChallengeSeasonDto;

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
                model.getEndDate());
    }
}
