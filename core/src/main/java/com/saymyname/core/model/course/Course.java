package com.saymyname.core.model.course;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.CourseStatus;
import com.saymyname.core.model.enums.CourseTargetScope;
import com.saymyname.core.model.enums.PopulationScope;
import com.saymyname.core.model.quiz.options.GameMode;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Course {
    Long id;
    Long userId;
    CourseTargetScope targetScope;
    Long targetAttributeId;
    CourseStatus status;
    int currentRound;
    PopulationScope populationScope;
    Instant createdAt;
    Instant updatedAt;
    Instant lastAccessedAt;

    // Backward-compatible accessor for legacy code paths.
    public User getUser() {
        return userId == null ? null : User.builder().id(userId).build();
    }

    // Backward-compatible accessor for legacy code paths.
    public GameMode getGameMode() {
        return targetAttributeId == null ? null : GameMode.builder().id(targetAttributeId).build();
    }
}
