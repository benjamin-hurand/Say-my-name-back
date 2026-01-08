package com.saymyname.persistence.dao.course;

import java.time.LocalDateTime;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.course.CourseRecentStats;
import com.saymyname.persistence.mapper.course.CourseRecentStatsEntityMapper;
import com.saymyname.persistence.repository.course.CourseRecentStatsRepository;

@Repository
@Transactional
public class CourseRecentStatsDao {

    private final CourseRecentStatsRepository repository;
    private final CourseRecentStatsEntityMapper mapper;

    public CourseRecentStatsDao(CourseRecentStatsRepository repository, CourseRecentStatsEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public CourseRecentStats findByCourseId(Long courseId) {
        if (courseId == null)
            return null;
        return repository.findByCourseId(courseId).map(mapper::toModel).orElse(null);
    }

    public int upsertOnAnswer(
            Long courseId,
            int errorStreak,
            int helpStreak,
            String lastFormat,
            int formatStreak,
            double avgRtRecent,
            LocalDateTime answeredAt,
            int correct,
            int helpUsed,
            int responseTimeMs,
            double decay) {
        return repository.upsertOnAnswer(
                courseId,
                errorStreak,
                helpStreak,
                lastFormat,
                formatStreak,
                avgRtRecent,
                answeredAt,
                correct,
                helpUsed,
                responseTimeMs,
                decay);
    }
}
