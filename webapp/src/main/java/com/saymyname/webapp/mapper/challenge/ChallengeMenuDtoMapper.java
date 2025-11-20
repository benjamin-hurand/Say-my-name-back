package com.saymyname.webapp.mapper.challenge;

import java.util.List;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.challenge.ChallengeMenu;
import com.saymyname.webapp.dto.challenge.ChallengeMenuDto;

@Component
public class ChallengeMenuDtoMapper {

    private final ChallengeFiltersDtoMapper challengeFiltersDtoMapper;
    private final ChallengeSortCriterionDtoMapper challengeSortCriterionDtoMapper;

    public ChallengeMenuDtoMapper(ChallengeFiltersDtoMapper challengeFiltersDtoMapper,
            ChallengeSortCriterionDtoMapper challengeSortCriterionDtoMapper) {
        this.challengeFiltersDtoMapper = challengeFiltersDtoMapper;
        this.challengeSortCriterionDtoMapper = challengeSortCriterionDtoMapper;
    }

    public ChallengeMenu toModel(ChallengeMenuDto dto) {
        if (dto == null)
            return null;

        List<com.saymyname.core.model.challenge.ChallengeSortCriterion> sorts = null;
        if (dto.sorts() != null) {
            sorts = dto.sorts().stream()
                    .map(challengeSortCriterionDtoMapper::toModel)
                    .toList();
        }

        // ⚠️ On ne met PAS userId ici (plus dans le DTO)
        ChallengeMenu menu = new ChallengeMenu();
        menu.setSeasonStart(dto.seasonStart());
        menu.setSearch(dto.search());
        menu.setFilters(challengeFiltersDtoMapper.toModel(dto.filters()));
        menu.setSorts(sorts);
        // menu.setUserId(null); // laissé à null : sera injecté en service
        return menu;
    }
}
