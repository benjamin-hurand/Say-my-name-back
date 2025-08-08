package com.saymyname.webapp.dto.profile;

public record AttributeStatsDto(
        int discoveredCount,
        int learnedCount,
        int masteredCount,
        int totalCount) {

}
