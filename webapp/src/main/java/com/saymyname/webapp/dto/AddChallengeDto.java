package com.saymyname.webapp.dto;

public record AddChallengeDto (
    String description,
    Long gameModeId,
    ReducedGameAttributeFilterDto attributeFilter,
    Long creatorId
) { }
