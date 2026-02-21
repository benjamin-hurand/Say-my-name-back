// src/main/java/com/saymyname/webapp/mapper/course/CourseDtoMapper.java
package com.saymyname.webapp.mapper.course;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.enums.PopulationScope;
import com.saymyname.core.model.enums.course.CourseStatus;
import com.saymyname.core.model.quiz.options.GameMode;
import com.saymyname.webapp.dto.course.CourseDto;
import com.saymyname.webapp.dto.course.CreateCourseDto;
import com.saymyname.webapp.mapper.UserDtoMapper;

@Component
public class CourseDtoMapper {

        private final UserDtoMapper userDtoMapper;

        public CourseDtoMapper(UserDtoMapper userDtoMapper) {
                this.userDtoMapper = userDtoMapper;
        }

        /** Model -> DTO (réponse API) */
        public CourseDto toDto(Course course) {
                return new CourseDto(
                                course.getId(),
                                course.getUser().getId(),
                                course.getGameMode().getId(),
                                course.getStatus(),
                                course.getCurrentRound());
        }

        /** DTO -> Model (si jamais utilisé en entrée) */
        public Course toModel(CourseDto courseDto) {
                return new Course.Builder()
                                .withId(courseDto.id())
                                .withUser(new User.Builder().withId(courseDto.userId()).build())
                                .withGameMode(new GameMode.Builder().withId(courseDto.gameModeId()).build())
                                .withStatus(courseDto.status())
                                .build();
        }

        /** Utilitaire existant (inchangé) */
        public Course toModel(Long courseId, Long userId) {
                return new Course.Builder()
                                .withId(courseId)
                                .withUser(userDtoMapper.toModel(userId))
                                .build();
        }

        /** CreateCourseDto -> Model pour la création */
        public Course toModel(CreateCourseDto dto) {
                return new Course.Builder()
                                .withGameMode(new GameMode.Builder().withId(dto.gameModeId()).build())
                                .withStatus(CourseStatus.IN_PROGRESS)
                                .withPopulationScope(dto.populationScope() != null ? dto.populationScope()
                                                : PopulationScope.FOLLOWED)
                                .build();
        }
}
