package com.saymyname.core.model.course;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class CourseRecentStats {
    Long id;
    Long courseId;
    int errorStreak;
    int helpStreak;
    LastFormat lastFormat;
    int formatStreak;
    double avgRtRecent;
    Instant lastAnswerAt;
    Instant createdAt;
    Instant updatedAt;

    public enum LastFormat {
        TEXT_INPUT,
        CLOZE,
        HANGMAN,
        MCQ,
        BINARY_SWIPE,
        ASSOCIATION,
        ORDERING
    }
}