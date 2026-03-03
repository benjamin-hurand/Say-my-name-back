// src/main/java/com/saymyname/webapp/mapper/course/CourseStatsDtoMapper.java
package com.saymyname.webapp.mapper.course;

import org.springframework.stereotype.Component;
import com.saymyname.core.model.course.CourseStats;
import com.saymyname.webapp.dto.course.CourseStatsDto;

@Component
public class CourseStatsDtoMapper {
        public CourseStatsDto toDto(CourseStats s) {
                return new CourseStatsDto(
                                s.getCourseId(),
                                s.getTargetAttributeId(),
                                s.getTotalCandidates(),
                                s.getUniverseEligible(),
                                s.getUnknown(),
                                s.getDiscovered(),
                                s.getLearned(),
                                s.getMastered(),
                                s.getTotalAnswers(),
                                s.getAnswersToday(),
                                s.getLastActivity(),
                                s.getCurrentRound(),
                                s.getDueNow());
        }
}
