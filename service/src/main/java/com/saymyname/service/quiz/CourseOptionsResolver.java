package com.saymyname.service.quiz;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.enums.FollowFilter;
import com.saymyname.core.model.enums.PopulationScope;
import com.saymyname.core.model.quiz.options.GameMode;
import com.saymyname.core.model.quiz.options.GameOptions;
import com.saymyname.service.GameModeService;

/**
 * Resolve GameOptions from a Course without silent placeholders.
 */
@Component
public class CourseOptionsResolver {

    private final GameModeService gameModeService;

    public CourseOptionsResolver(GameModeService gameModeService) {
        this.gameModeService = Objects.requireNonNull(gameModeService, "gameModeService");
    }

    public GameOptions resolve(Course course) {
        Objects.requireNonNull(course, "course");

        GameMode gameMode = requireGameMode(course);
        FollowFilter scope = mapScope(course.getPopulationScope());

        return new GameOptions.Builder()
                .withId(course.getId())
                .withGameMode(gameMode)
                .withPopulationScope(scope)
                .withFilters(List.of())
                .withSortBy(List.of())
                .withTrackKnowledge(true)
                .build();
    }

    private GameMode requireGameMode(Course course) {
        GameMode gameMode = course.getGameMode();
        if (gameMode == null || gameMode.getId() == null) {
            throw new IllegalStateException("Course gameMode is required");
        }
        if (gameMode.getGameModeAttributes() == null || gameMode.getGameModeAttributes().isEmpty()
                || gameMode.getOperator() == null || gameMode.getOperator().isBlank()) {
            return gameModeService.findByIdOrThrow(gameMode.getId());
        }
        return gameMode;
    }

    private FollowFilter mapScope(PopulationScope scope) {
        if (scope == null) {
            return FollowFilter.FOLLOWED;
        }
        return switch (scope) {
            case ALL -> FollowFilter.ALL;
            case FOLLOWED -> FollowFilter.FOLLOWED;
        };
    }
}
