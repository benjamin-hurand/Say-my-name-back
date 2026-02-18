// src/main/java/com/saymyname/persistence/mapper/course/CourseEntityMapper.java
package com.saymyname.persistence.mapper.course;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.enums.CourseStatus;
import com.saymyname.core.model.enums.CourseTargetScope;
import com.saymyname.core.model.enums.PopulationScope;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.course.CourseEntity;

@Component
public class CourseEntityMapper {

    @Autowired
    public CourseEntityMapper() {
    }

    public CourseEntity toEntity(Course model) {
        if (model == null)
            return null;

        CourseEntity e = CourseEntity.builder().build();
        e.setId(model.getId());
        if (model.getUserId() != null) {
            e.setUser(UserEntity.builder().id(model.getUserId()).build());
        }
        e.setTargetScope(model.getTargetScope() != null
                ? model.getTargetScope()
                : CourseTargetScope.ATTRIBUTE);
        e.setTargetAttributeId(model.getTargetAttributeId());
        e.setStatus(model.getStatus() != null ? model.getStatus() : CourseStatus.IN_PROGRESS);
        e.setCurrentRound(model.getCurrentRound());
        e.setPopulationScope(model.getPopulationScope() != null
                ? model.getPopulationScope()
                : PopulationScope.FOLLOWED);
        e.setLastAccessedAt(toLocalDateTime(model.getLastAccessedAt()));
        return e;
    }

    public Course toModel(CourseEntity e) {
        if (e == null)
            return null;

        return Course.builder()
                .id(e.getId())
                .userId(e.getUser() != null ? e.getUser().getId() : null)
                .targetScope(e.getTargetScope())
                .targetAttributeId(e.getTargetAttributeId())
                .status(e.getStatus())
                .currentRound(e.getCurrentRound())
                .populationScope(e.getPopulationScope())
                .createdAt(toInstant(e.getCreatedAt()))
                .updatedAt(toInstant(e.getUpdatedAt()))
                .lastAccessedAt(toInstant(e.getLastAccessedAt()))
                .build();
    }

    public Course toShortModel(CourseEntity e) {
        return toModel(e);
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
