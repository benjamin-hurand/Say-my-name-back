package com.saymyname.webapp.dto.challenge;

public record ChallengeGameModeDto(
                Long id,
                String title, // getGameModeTitle()
                String description // getGameModeDescription()
) {
}
