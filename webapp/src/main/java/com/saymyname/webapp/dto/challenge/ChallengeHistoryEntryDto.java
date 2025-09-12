package com.saymyname.webapp.dto.challenge;

public record ChallengeHistoryEntryDto(
                Integer questionNumber,
                Long personId,
                String answer) {

}
