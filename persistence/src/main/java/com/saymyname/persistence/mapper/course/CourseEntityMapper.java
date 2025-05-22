package com.saymyname.persistence.mapper.course;

import com.saymyname.core.model.course.Course;
import com.saymyname.persistence.entity.course.CourseEntity;
import com.saymyname.persistence.mapper.AttributeEntityMapper;
import com.saymyname.persistence.mapper.GameModeEntityMapper;
import com.saymyname.persistence.mapper.UserEntityMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CourseEntityMapper {

    private final UserEntityMapper userMapper;
    private final GameModeEntityMapper gameModeMapper;
    private final AttributeEntityMapper attributeMapper;
    private final PopulationEntityMapper populationMapper;

    @Autowired
    public CourseEntityMapper(UserEntityMapper userMapper,
            GameModeEntityMapper gameModeMapper,
            AttributeEntityMapper attributeMapper,
            PopulationEntityMapper populationMapper) {
        this.userMapper = userMapper;
        this.gameModeMapper = gameModeMapper;
        this.attributeMapper = attributeMapper;
        this.populationMapper = populationMapper;
    }

    public CourseEntity toEntity(Course model) {
        if (model == null)
            return null;
        CourseEntity e = new CourseEntity();
        e.setId(model.getId());
        e.setUser(userMapper.toEntity(model.getUser()));
        e.setGameMode(gameModeMapper.toEntity(model.getGameMode()));
        e.setSortingAttribute(attributeMapper.toEntity(model.getSortingAttribute()));
        e.setSortingOrder(model.getSortingOrder());
        e.setStatus(model.getStatus());
        e.setCurrentRound(model.getCurrentRound());
        e.setPopulations(
                model.getPopulations().stream()
                        .map(populationMapper::toEntity)
                        .collect(Collectors.toList()));
        return e;
    }

    public Course toModel(CourseEntity e) {
        if (e == null)
            return null;
        return new Course.Builder()
                .withId(e.getId())
                .withUser(userMapper.toModel(e.getUser()))
                .withGameMode(gameModeMapper.toModel(e.getGameMode()))
                .withSortingAttribute(attributeMapper.toModel(e.getSortingAttribute()))
                .withSortingOrder(e.getSortingOrder())
                .withStatus(e.getStatus())
                .withCurrentRound(e.getCurrentRound())
                .withPopulations(e.getPopulations().stream()
                        .map(populationMapper::toModel)
                        .collect(Collectors.toList()))
                .build();
    }
}
