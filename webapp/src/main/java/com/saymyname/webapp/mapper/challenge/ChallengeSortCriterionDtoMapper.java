package com.saymyname.webapp.mapper.challenge;

import org.springframework.stereotype.Component;
import com.saymyname.core.model.challenge.ChallengeSortCriterion;
import com.saymyname.webapp.dto.challenge.ChallengeSortCriterionDto;

@Component
public class ChallengeSortCriterionDtoMapper {

    public ChallengeSortCriterion toModel(ChallengeSortCriterionDto dto) {
        if (dto == null)
            return null;
        return new ChallengeSortCriterion(
                dto.type(),
                dto.order());
    }
}
