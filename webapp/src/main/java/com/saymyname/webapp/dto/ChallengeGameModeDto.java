package com.saymyname.webapp.dto;

public record ChallengeGameModeDto(
        Long id,
        String title, // getGameModeTitle()
        String description // getGameModeDescription()
) {
}
