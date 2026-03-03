// src/main/java/com/saymyname/persistence/mapper/course/CourseEntityMapper.java
package com.saymyname.persistence.mapper.course;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.enums.PopulationScope;
import com.saymyname.core.model.enums.course.CourseStatus;
import com.saymyname.persistence.entity.organization.course.CourseEntity;
import com.saymyname.persistence.mapper.UserEntityMapper;

@Component
public class CourseEntityMapper {

    private final UserEntityMapper userMapper;

    @Autowired
    public CourseEntityMapper(UserEntityMapper userMapper) {
        this.userMapper = userMapper;
    }

    public CourseEntity toEntity(Course model) {
        if (model == null)
            return null;

        CourseEntity e = new CourseEntity();
        e.setId(model.getId());
        e.setUser(userMapper.toEntity(model.getUser()));

        e.setTargetAttributeId(model.getTargetAttributeId());

        e.setStatus(model.getStatus() != null ? model.getStatus() : CourseStatus.IN_PROGRESS);
        e.setCurrentRound(model.getCurrentRound());
        e.setPopulationScope(
                model.getPopulationScope() != null ? model.getPopulationScope() : PopulationScope.FOLLOWED);

        e.setCreatedAt(model.getCreatedAt());
        e.setUpdatedAt(model.getUpdatedAt());
        e.setLastAccessedAt(model.getLastAccessedAt());
        return e;
    }

    public Course toModel(CourseEntity e) {
        if (e == null)
            return null;

        return new Course.Builder()
                .withId(e.getId())
                .withUser(userMapper.toShortModel(e.getUser()))
                .withTargetAttributeId(e.getTargetAttributeId())
                .withStatus(e.getStatus())
                .withCurrentRound(e.getCurrentRound())
                .withPopulationScope(e.getPopulationScope())
                .withCreatedAt(e.getCreatedAt())
                .withUpdatedAt(e.getUpdatedAt())
                .withLastAccessedAt(e.getLastAccessedAt())
                .build();
    }

    public Course toShortModel(CourseEntity e) {
        if (e == null)
            return null;

        return new Course.Builder()
                .withId(e.getId())
                .withUser(userMapper.toShortModel(e.getUser()))
                .withTargetAttributeId(e.getTargetAttributeId())
                .withStatus(e.getStatus())
                .withCurrentRound(e.getCurrentRound())
                .withPopulationScope(e.getPopulationScope())
                .withCreatedAt(e.getCreatedAt())
                .withUpdatedAt(e.getUpdatedAt())
                .withLastAccessedAt(e.getLastAccessedAt())
                .build();
    }
}