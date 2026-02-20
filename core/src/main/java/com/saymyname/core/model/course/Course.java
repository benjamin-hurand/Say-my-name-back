package com.saymyname.core.model.course;

import java.time.Instant;

import com.saymyname.core.model.enums.CourseStatus;
import com.saymyname.core.model.enums.CourseTargetScope;
import com.saymyname.core.model.enums.PopulationScope;

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
}
