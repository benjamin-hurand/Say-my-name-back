package com.saymyname.core.model.course;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class KnowledgeStats {
    Long id;
    Long userId;
    Long factId;
    Long knowledgeId;
    double attemptsRecent;
    double correctRecent;
    double helpRecent;
    double avgRtRecent;
    Instant lastAnswerAt;
    Boolean lastCorrect;
    Boolean lastHelpUsed;
    int lastResponseTimeMs;
    int errorStreak;
    Instant createdAt;
    Instant updatedAt;
}