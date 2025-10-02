package com.saymyname.persistence.mapper;

import org.springframework.stereotype.Component;
import com.saymyname.core.model.challenge.ChallengeSeason;
import com.saymyname.persistence.entity.organization.ChallengeSeasonEntity;

@Component
public class ChallengeSeasonEntityMapper {

    public ChallengeSeasonEntity toEntity(ChallengeSeason model) {
        if (model == null) {
            return null;
        }
        return new ChallengeSeasonEntity.Builder()
                .withId(model.getId())
                .withSeasonNumber(model.getSeasonNumber())
                .withStartDate(model.getStartDate())
                .withEndDate(model.getEndDate())
                .build();
    }

    public ChallengeSeason toModel(ChallengeSeasonEntity entity) {
        if (entity == null) {
            return null;
        }
        return new ChallengeSeason.Builder()
                .withId(entity.getId())
                .withSeasonNumber(entity.getSeasonNumber())
                .withStartDate(entity.getStartDate())
                .withEndDate(entity.getEndDate())
                .build();
    }
}
