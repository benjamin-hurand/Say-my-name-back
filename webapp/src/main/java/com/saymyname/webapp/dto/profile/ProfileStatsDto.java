package com.saymyname.webapp.dto.profile;

public record ProfileStatsDto(
        AttributeStatsDto attributeStats,
        ChallengeStatsDto challengeStats,
        TrainingStatsDto trainingStats) {

}
