package com.saymyname.webapp.dto.challenge;

import java.time.LocalDateTime;

import com.saymyname.webapp.dto.GameAttributeFilterDto;
import com.saymyname.webapp.dto.GameModeDto;
import com.saymyname.webapp.dto.ReducedUserDto;

public record ChallengeDto(
                Long id,
                String description,
                GameModeDto gameMode,
                GameAttributeFilterDto attributeFilter,
                LocalDateTime creationDate,
                ReducedUserDto creator) {

}
