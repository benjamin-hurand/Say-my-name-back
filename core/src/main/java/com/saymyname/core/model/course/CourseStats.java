package com.saymyname.core.model.course;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class CourseStats {
    Long courseId;
    Long targetAttributeId;
    long totalCandidates;
    long universeEligible;
    long unknown;
    long discovered;
    long learned;
    long mastered;
    long totalAnswers;
    long answersToday;
    Instant lastActivity;
    int currentRound;
    long dueNow;
}
