// src/main/java/com/saymyname/webapp/dto/course/CourseStatsDto.java
package com.saymyname.webapp.dto.course;

import java.time.LocalDateTime;

public record CourseStatsDto(
        Long courseId,
        Long targetAttributeId,
        long totalCandidates,
        long universeEligible,
        long unknown,
        long discovered,
        long learned,
        long mastered,
        long totalAnswers,
        long answersToday,
        LocalDateTime lastActivity,
        int currentRound,
        long dueNow) {
}
