// src/main/java/com/saymyname/persistence/mapper/course/CourseEntityMapper.java
package com.saymyname.persistence.mapper.course;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.enums.CourseStatus;
import com.saymyname.core.model.enums.PopulationScope;
import com.saymyname.core.model.game.options.GameMode;
import com.saymyname.persistence.entity.course.CourseEntity;
import com.saymyname.persistence.mapper.GameModeEntityMapper;
import com.saymyname.persistence.mapper.UserEntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CourseEntityMapper {

    private final UserEntityMapper userMapper;
    private final GameModeEntityMapper gameModeMapper;

    @Autowired
    public CourseEntityMapper(UserEntityMapper userMapper,
            GameModeEntityMapper gameModeMapper) {
        this.userMapper = userMapper;
        this.gameModeMapper = gameModeMapper;
    }

    public CourseEntity toEntity(Course model) {
        if (model == null)
            return null;

        CourseEntity e = new CourseEntity();
        e.setId(model.getId());
        e.setUser(userMapper.toEntity(model.getUser()));
        e.setGameMode(gameModeMapper.toEntity(model.getGameMode()));
        e.setStatus(model.getStatus() != null ? model.getStatus() : CourseStatus.IN_PROGRESS);
        e.setCurrentRound(model.getCurrentRound());
        e.setPopulationScope(
                model.getPopulationScope() != null ? model.getPopulationScope() : PopulationScope.FOLLOWED);
        return e;
    }

    public Course toModel(CourseEntity e) {
        if (e == null)
            return null;

        return new Course.Builder()
                .withId(e.getId())
                .withUser(new User.Builder().withId(
                        e.getUser() != null ? e.getUser().getId() : null).build())
                .withGameMode(new GameMode.Builder().withId(
                        e.getGameMode() != null ? e.getGameMode().getId() : null).build())
                .withStatus(e.getStatus())
                .withCurrentRound(e.getCurrentRound())
                .withPopulationScope(e.getPopulationScope())
                .build();
    }
}
