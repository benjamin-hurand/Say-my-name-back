package com.saymyname.webapp.dto.challenge;

import com.saymyname.core.model.enums.ChallengeSortCriterionType;
import com.saymyname.core.model.enums.OrderDirection;

public record ChallengeSortCriterionDto(
        ChallengeSortCriterionType type, // Ex: ENUM "Popularité", "Longueur", etc.
        OrderDirection order // Ex: ENUM "asc" ou "desc"
) {
}
