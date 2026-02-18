package com.saymyname.core.model.course;

import com.saymyname.core.model.enums.PoolType;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class CourseQuestionAttempt {
    Long id;
    Long courseId;
    int questionRound;
    Instant askedAt;
    Instant answeredAt;
    int responseTimeMs;
    String rawSubmission;
    String normalizedAudit;
    boolean globalCorrect;
    PoolType poolType;
    boolean helpUsed;
    QuestionFormat questionFormat;
    int snapshotSchemaVersion;
    String generatorVersion;
    String normalizerVersion;
    String questionSnapshotJson;
    String stepStateJson;
    PlannedFormat plannedFormat;
    boolean plannedTimed;
    Integer plannedTimeLimitMs;
    int plannedTargetCount;
    String plannedTargetKnowledgeIdsJson;
    String plannedParamsJson;
    String plannedReasonCode;
    String plannedReasonDetailsJson;

    public enum QuestionFormat {
        TEXT_INPUT,
        CLOZE,
        HANGMAN,
        MCQ,
        BINARY_SWIPE,
        ASSOCIATION,
        ORDERING
    }

    public enum PlannedFormat {
        AUTO,
        TEXT_INPUT,
        CLOZE,
        HANGMAN,
        WORD_PUZZLE,
        MCQ,
        BINARY_SWIPE,
        ASSOCIATION,
        ORDERING,
        SPEED
    }
}