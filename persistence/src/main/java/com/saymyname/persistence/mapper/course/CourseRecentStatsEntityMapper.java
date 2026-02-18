package com.saymyname.persistence.mapper.course;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.CourseRecentStats;
import com.saymyname.persistence.entity.organization.course.CourseRecentStatsEntity;

@Component
public class CourseRecentStatsEntityMapper {

    public CourseRecentStatsEntity toEntity(CourseRecentStats model) {
        if (model == null)
            return null;

        CourseRecentStatsEntity entity = CourseRecentStatsEntity.builder().build();
        entity.setId(model.getId());
        entity.setCourseId(model.getCourseId());
        entity.setErrorStreak(model.getErrorStreak());
        entity.setHelpStreak(model.getHelpStreak());
        entity.setLastFormat(toEntityLastFormat(model.getLastFormat()));
        entity.setFormatStreak(model.getFormatStreak());
        entity.setAvgRtRecent(model.getAvgRtRecent());
        entity.setLastAnswerAt(toLocalDateTime(model.getLastAnswerAt()));
        entity.setCreatedAt(toLocalDateTime(model.getCreatedAt()));
        entity.setUpdatedAt(toLocalDateTime(model.getUpdatedAt()));
        return entity;
    }

    public CourseRecentStats toModel(CourseRecentStatsEntity entity) {
        if (entity == null)
            return null;

        return CourseRecentStats.builder()
                .id(entity.getId())
                .courseId(entity.getCourseId())
                .errorStreak(entity.getErrorStreak())
                .helpStreak(entity.getHelpStreak())
                .lastFormat(toModelLastFormat(entity.getLastFormat()))
                .formatStreak(entity.getFormatStreak())
                .avgRtRecent(entity.getAvgRtRecent())
                .lastAnswerAt(toInstant(entity.getLastAnswerAt()))
                .createdAt(toInstant(entity.getCreatedAt()))
                .updatedAt(toInstant(entity.getUpdatedAt()))
                .build();
    }

    private CourseRecentStatsEntity.LastFormat toEntityLastFormat(CourseRecentStats.LastFormat value) {
        return value == null ? null : CourseRecentStatsEntity.LastFormat.valueOf(value.name());
    }

    private CourseRecentStats.LastFormat toModelLastFormat(CourseRecentStatsEntity.LastFormat value) {
        return value == null ? null : CourseRecentStats.LastFormat.valueOf(value.name());
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
