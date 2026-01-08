package com.saymyname.service.course;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.course.CourseRecentStats;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.persistence.dao.course.CourseRecentStatsDao;

@Service
public class CourseRecentStatsService {

    private static final double DECAY = 0.90;

    private final CourseRecentStatsDao dao;

    public CourseRecentStatsService(CourseRecentStatsDao dao) {
        this.dao = dao;
    }

    @Transactional(readOnly = true)
    public CourseRecentStats getStatsForCourse(Long courseId) {
        return dao.findByCourseId(courseId);
    }

    @Transactional
    public void upsertOnAnswer(
            Long courseId,
            QuizFormat format,
            boolean correct,
            boolean helpUsed,
            int responseTimeMs,
            LocalDateTime answeredAt) {
        if (courseId == null) {
            return;
        }

        int errorStreak = correct ? 0 : 1;
        int helpStreak = helpUsed ? 1 : 0;
        int formatStreak = 1;
        String lastFormat = format != null ? format.name() : null;
        int safeResponseTimeMs = Math.max(0, responseTimeMs);
        LocalDateTime at = answeredAt != null ? answeredAt : LocalDateTime.now();

        dao.upsertOnAnswer(
                courseId,
                errorStreak,
                helpStreak,
                lastFormat,
                formatStreak,
                safeResponseTimeMs,
                at,
                correct ? 1 : 0,
                helpUsed ? 1 : 0,
                safeResponseTimeMs,
                DECAY);
    }
}
