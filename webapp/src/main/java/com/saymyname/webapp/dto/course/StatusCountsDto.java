package com.saymyname.webapp.dto.course;

public record StatusCountsDto(
        Integer unknown,
        Integer discovered,
        Integer learned,
        Integer mastered) {
}
