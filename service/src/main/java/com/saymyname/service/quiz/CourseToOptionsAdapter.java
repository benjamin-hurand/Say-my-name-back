// src/main/java/com/saymyname/service/quiz/CourseToOptionsAdapter.java
package com.saymyname.service.quiz;

import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.quiz.options.GameMode;
import com.saymyname.core.model.quiz.options.GameOptions;

/**
 * Temporary adapter: build a GameOptions-like object from a Course.
 * Replace later with a proper "CourseOptionsResolver".
 */
public final class CourseToOptionsAdapter {

    private CourseToOptionsAdapter() {
    }

    public static GameOptions toGameOptions(Course course) {
        return new GameOptions.Builder()
                .withId(course.getId()) // not perfect but ok for "reducedOptionsId" in context
                .withGameMode(course.getGameMode())
                .build();
    }

    /**
     * TEMP: you need a real GameModeService in production.
     * TODO
     * Here just build an id-only stub so KnowledgeService doesn't NPE on
     * gameMode.getId().
     * WARNING: validateAnswer needs gameModeAttributes; so this MUST be replaced by
     * real loading.
     */
    public static GameMode loadGameMode(Long gameModeId) {
        return new GameMode.Builder().withId(gameModeId).build();
    }
}
