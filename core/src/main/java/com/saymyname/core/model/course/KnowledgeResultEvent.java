package com.saymyname.core.model.course;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class KnowledgeResultEvent {
    Long knowledgeId;
    Long gameModeId;
    Long personId;
    Long factId;
    boolean correct;
    boolean helpUsed;
    Long courseId;
    Long courseQuestionAttemptId;
    Integer questionRound;
    Instant occurredAt;
}
