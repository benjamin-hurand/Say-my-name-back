// src/main/java/com/saymyname/persistence/mapper/course/CourseEntityMapper.java
package com.saymyname.persistence.mapper.course;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.enums.CourseStatus;
import com.saymyname.core.model.enums.PopulationScope;
import com.saymyname.persistence.entity.organization.course.CourseEntity;
import com.saymyname.persistence.mapper.GameModeEntityMapper;
import com.saymyname.persistence.mapper.UserEntityMapper;

@Component
public class CourseEntityMapper {

    private final UserEntityMapper userMapper;
    private final GameModeEntityMapper gameModeMapper;

    @Autowired
    public CourseEntityMapper(UserEntityMapper userMapper, GameModeEntityMapper gameModeMapper) {
        this.userMapper = userMapper;
        this.gameModeMapper = gameModeMapper;
    }

    public CourseEntity toEntity(Course model) {
        if (model == null)
            return null;

        CourseEntity e = new CourseEntity();
        e.setId(model.getId());
        e.setUser(userMapper.toEntity(model.getUser())); // idem, potentiellement même souci
        // ❌ e.setGameMode(gameModeMapper.toRefEntity(model.getGameMode()));
        e.setStatus(model.getStatus() != null ? model.getStatus() : CourseStatus.IN_PROGRESS);
        e.setCurrentRound(model.getCurrentRound());
        e.setPopulationScope(
                model.getPopulationScope() != null ? model.getPopulationScope() : PopulationScope.FOLLOWED);
        e.setLastAccessedAt(model.getLastAccessedAt());
        return e;
    }

    public Course toModel(CourseEntity e) {
        if (e == null)
            return null;

        return new Course.Builder()
                .withId(e.getId())
                .withUser(userMapper.toShortModel(e.getUser()))
                .withGameMode(gameModeMapper.toModel(e.getGameMode()))
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
                .withGameMode(gameModeMapper.toShortModel(e.getGameMode()))
                .withStatus(e.getStatus())
                .withCurrentRound(e.getCurrentRound())
                .withPopulationScope(e.getPopulationScope())
                .withCreatedAt(e.getCreatedAt())
                .withUpdatedAt(e.getUpdatedAt())
                .withLastAccessedAt(e.getLastAccessedAt())
                .build();
    }
}
