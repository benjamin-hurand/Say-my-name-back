// src/main/java/com/saymyname/webapp/mapper/course/CourseStatsDtoMapper.java
package com.saymyname.webapp.mapper.course;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.CourseStats;
import com.saymyname.webapp.dto.course.CourseStatsDto;

@Component
public class CourseStatsDtoMapper {

    public CourseStatsDto toDto(CourseStats s) {
        long totalCandidates = Math.max(0, s.getTotalCandidates());
        int createdTotal = Math.max(0, s.getCreatedTotal());

        int knownTotal = createdTotal;
        int remainingUnseen = (int) Math.max(0, totalCandidates - createdTotal);

        double createdCoverage = totalCandidates > 0
                ? (double) createdTotal / (double) totalCandidates
                : 0.0;

        double masteredRatio = createdTotal > 0
                ? (double) s.getMastered() / (double) createdTotal
                : 0.0;

        int progressPercent = (int) Math.round(masteredRatio * 100.0);

        boolean finished = createdTotal > 0 && s.getMastered() == createdTotal;

        int canGrowBy = (int) Math.max(0, s.getTotalPersonsGlobal() - totalCandidates);

        // dueNow non calculé dans le modèle actuel → null
        Integer dueNow = null;

        return new CourseStatsDto(
                s.getCourseId(),
                s.getUserId(),
                s.getGameModeId(),
                s.getPopulationScope(),

                s.getUnknown(),
                s.getDiscovered(),
                s.getLearned(),
                s.getMastered(),
                createdTotal,

                totalCandidates,
                s.getTotalPersonsGlobal(),

                knownTotal,
                remainingUnseen,
                progressPercent,
                createdCoverage,
                masteredRatio,
                finished,

                s.getTotalAnswers(),
                s.getAnswersToday(),
                s.getLastActivity(),
                s.getCurrentRound(),

                dueNow,
                canGrowBy);
    }
}
