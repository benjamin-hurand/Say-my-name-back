package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.challenge.ChallengeFilters;
import com.saymyname.core.model.game.options.GameAttributeFilter;
import com.saymyname.webapp.dto.ChallengeFiltersDto;

@Component
public class ChallengeFiltersDtoMapper {

    private final GameAttributeFilterDtoMapper gameAttributeFilterDtoMapper;

    public ChallengeFiltersDtoMapper(GameAttributeFilterDtoMapper gameAttributeFilterDtoMapper) {
        this.gameAttributeFilterDtoMapper = gameAttributeFilterDtoMapper;
    }

    public ChallengeFilters toModel(ChallengeFiltersDto dto) {
        if (dto == null)
            return null;
        GameAttributeFilter attributeFilter = null;
        if (dto.attributeFilter() != null) {
            attributeFilter = gameAttributeFilterDtoMapper.toModel(dto.attributeFilter());
        }
        return new ChallengeFilters(
                dto.gameModeIds(),
                dto.userPerformances(),
                attributeFilter,
                dto.participantsRangeMin(),
                dto.participantsRangeMax(),
                dto.questionsRangeMin(),
                dto.questionsRangeMax(),
                dto.dateRangeMin(),
                dto.dateRangeMax());
    }
}
