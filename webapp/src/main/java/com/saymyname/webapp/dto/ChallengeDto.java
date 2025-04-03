package com.saymyname.webapp.dto;

import java.time.LocalDateTime;

public record ChallengeDto (
    Long id,
    String description,
    GameModeDto gameMode,
    GameAttributeFilterDto attributeFilter,
    LocalDateTime creationDate,
    UserDto creator
)
{

}
