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
                ? toEntityTargetScope(model.getTargetScope())
                : CourseEntity.CourseTargetScope.ATTRIBUTE);
        e.setTargetAttributeId(model.getTargetAttributeId());
        e.setStatus(model.getStatus() != null ? toEntityStatus(model.getStatus()) : CourseEntity.CourseStatus.IN_PROGRESS);
        e.setCurrentRound(model.getCurrentRound());
        e.setPopulationScope(model.getPopulationScope() != null
                ? toEntityPopulationScope(model.getPopulationScope())
                : CourseEntity.PopulationScope.FOLLOWED);
        e.setLastAccessedAt(toLocalDateTime(model.getLastAccessedAt()));
        return e;
    }

    public Course toModel(CourseEntity e) {
        if (e == null)
            return null;

        return Course.builder()
                .id(e.getId())
                .userId(e.getUser() != null ? e.getUser().getId() : null)
                .targetScope(toModelTargetScope(e.getTargetScope()))
                .targetAttributeId(e.getTargetAttributeId())
                .status(toModelStatus(e.getStatus()))
                .currentRound(e.getCurrentRound())
                .populationScope(toModelPopulationScope(e.getPopulationScope()))
                .createdAt(toInstant(e.getCreatedAt()))
                .updatedAt(toInstant(e.getUpdatedAt()))
                .lastAccessedAt(toInstant(e.getLastAccessedAt()))
                .build();
    }

    public Course toShortModel(CourseEntity e) {
        return toModel(e);
    }

    private CourseEntity.CourseTargetScope toEntityTargetScope(CourseTargetScope value) {
        return value == null ? null : CourseEntity.CourseTargetScope.valueOf(value.name());
    }

    private CourseTargetScope toModelTargetScope(CourseEntity.CourseTargetScope value) {
        return value == null ? null : CourseTargetScope.valueOf(value.name());
    }

    private CourseEntity.CourseStatus toEntityStatus(CourseStatus value) {
        return value == null ? null : CourseEntity.CourseStatus.valueOf(value.name());
    }

    private CourseStatus toModelStatus(CourseEntity.CourseStatus value) {
        return value == null ? null : CourseStatus.valueOf(value.name());
    }

    private CourseEntity.PopulationScope toEntityPopulationScope(PopulationScope value) {
        return value == null ? null : CourseEntity.PopulationScope.valueOf(value.name());
    }

    private PopulationScope toModelPopulationScope(CourseEntity.PopulationScope value) {
        return value == null ? null : PopulationScope.valueOf(value.name());
    }

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
