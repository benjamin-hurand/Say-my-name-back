package com.saymyname.webapp.dto;

public record ChallengeHistoryEntryDto(
        Integer questionNumber,
        Long personId,
        String answer) {

}
