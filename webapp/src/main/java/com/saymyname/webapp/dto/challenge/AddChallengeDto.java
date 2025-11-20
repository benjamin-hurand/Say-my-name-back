package com.saymyname.webapp.dto.challenge;

import com.saymyname.webapp.dto.ReducedGameAttributeFilterDto;

public record AddChallengeDto(
                String description,
                Long gameModeId,
                ReducedGameAttributeFilterDto attributeFilter) {
}
