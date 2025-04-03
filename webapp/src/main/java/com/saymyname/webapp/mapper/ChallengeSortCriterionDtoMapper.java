package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;
import com.saymyname.core.model.challenge.ChallengeSortCriterion;
import com.saymyname.webapp.dto.ChallengeSortCriterionDto;

@Component
public class ChallengeSortCriterionDtoMapper {

    public ChallengeSortCriterion toModel(ChallengeSortCriterionDto dto) {
        if(dto == null) return null;
        return new ChallengeSortCriterion(
            dto.type(),
            dto.order()
        );
    }
}
