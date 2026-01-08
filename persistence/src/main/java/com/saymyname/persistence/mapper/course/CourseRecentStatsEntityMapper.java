package com.saymyname.persistence.mapper.course;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.CourseRecentStats;
import com.saymyname.persistence.entity.organization.course.CourseEntity;
import com.saymyname.persistence.entity.organization.course.CourseRecentStatsEntity;

@Component
public class CourseRecentStatsEntityMapper {

    public CourseRecentStatsEntity toEntity(CourseRecentStats model) {
        if (model == null)
            return null;

        CourseRecentStatsEntity entity = new CourseRecentStatsEntity();
        entity.setId(model.getId());

        if (model.getCourseId() != null) {
            CourseEntity courseRef = new CourseEntity();
            courseRef.setId(model.getCourseId());
            entity.setCourse(courseRef);
        }

        entity.setErrorStreak(model.getErrorStreak());
        entity.setHelpStreak(model.getHelpStreak());
        entity.setLastFormat(model.getLastFormat());
        entity.setFormatStreak(model.getFormatStreak());
        entity.setAvgRtRecent(model.getAvgRtRecent());
        entity.setLastAnswerAt(model.getLastAnswerAt());
        entity.setCreatedAt(model.getCreatedAt());
        entity.setUpdatedAt(model.getUpdatedAt());

        return entity;
    }

    public CourseRecentStats toModel(CourseRecentStatsEntity entity) {
        if (entity == null)
            return null;

        return new CourseRecentStats.Builder()
                .withId(entity.getId())
                .withCourseId(entity.getCourse() != null ? entity.getCourse().getId() : null)
                .withErrorStreak(entity.getErrorStreak())
                .withHelpStreak(entity.getHelpStreak())
                .withLastFormat(entity.getLastFormat())
                .withFormatStreak(entity.getFormatStreak())
                .withAvgRtRecent(entity.getAvgRtRecent())
                .withLastAnswerAt(entity.getLastAnswerAt())
                .withCreatedAt(entity.getCreatedAt())
                .withUpdatedAt(entity.getUpdatedAt())
                .build();
    }
}
