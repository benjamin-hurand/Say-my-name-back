package com.saymyname.webapp.mapper.course;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.common.User;
import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.enums.CourseStatus;
import com.saymyname.core.model.game.options.GameMode;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.webapp.dto.course.CourseDto;
import com.saymyname.webapp.dto.course.CreateCourseDto;
import com.saymyname.webapp.mapper.UserDtoMapper;

@Component
public class CourseDtoMapper {

        private final PopulationDtoMapper populationDtoMapper;
        private final UserDtoMapper userDtoMapper;

        public CourseDtoMapper(PopulationDtoMapper populationDtoMapper, UserDtoMapper userDtoMapper) {
                this.populationDtoMapper = populationDtoMapper;
                this.userDtoMapper = userDtoMapper;
        }

        public CourseDto toDto(Course course) {
                return new CourseDto(
                                course.getId(),
                                course.getUser().getId(),
                                course.getGameMode().getId(),
                                course.getSortingAttribute().getId(),
                                course.getSortingOrder(),
                                course.getStatus(),
                                course.getPopulations().stream()
                                                .map(population -> population.getId())
                                                .toList());
        }

        public Course toModel(CourseDto courseDto) {
                return new Course.Builder()
                                .withId(courseDto.id())
                                .withUser(new User.Builder().withId(courseDto.userId()).build())
                                .withGameMode(new GameMode.Builder().withId(courseDto.gameModeId()).build())
                                .withSortingAttribute(new Attribute.Builder()
                                                .withId(courseDto.sortingMethodAttributeId()).build())
                                .withSortingOrder(courseDto.sortingOrder())
                                .withPopulations(courseDto.populationIds().stream()
                                                .map(populationDtoMapper::toModel)
                                                .toList())
                                .withStatus(courseDto.status())
                                .build();
        }

        public Course toModel(Long courseId, Long userId) {
                return new Course.Builder()
                                .withId(courseId)
                                .withUser(userDtoMapper.toModel(userId))
                                .build();
        }

        public Course toModel(CreateCourseDto dto) {
                return new Course.Builder()
                                .withUser(new User.Builder().withId(dto.userId()).build())
                                .withGameMode(new GameMode.Builder().withId(dto.gameModeId()).build())
                                .withSortingAttribute(new Attribute.Builder().withId(dto.sortingAttributeId()).build())
                                .withSortingOrder(dto.sortingOrder())
                                .withStatus(CourseStatus.IN_PROGRESS)
                                .withPopulations(dto.populationIds().stream()
                                                .map(populationDtoMapper::toModel)
                                                .toList())
                                .build();
        }
}
