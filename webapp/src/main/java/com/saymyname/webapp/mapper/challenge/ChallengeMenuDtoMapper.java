package com.saymyname.webapp.mapper.challenge;

import java.util.List;
import java.util.stream.Collectors;

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
        if (dto == null) {
            return null;
        }
        List<com.saymyname.core.model.challenge.ChallengeSortCriterion> sorts = null;
        if (dto.sorts() != null) {
            sorts = dto.sorts().stream()
                    .map(challengeSortCriterionDtoMapper::toModel)
                    .collect(Collectors.toList());
        }
        return new ChallengeMenu(
                dto.userId(),
                dto.seasonStart(), // Utilise directement la date
                dto.search(),
                challengeFiltersDtoMapper.toModel(dto.filters()),
                sorts);
    }
}
