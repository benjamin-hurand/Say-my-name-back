package com.saymyname.core.model.course;

import com.saymyname.core.model.enums.KnowledgeStatus;
import java.math.BigDecimal;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Knowledge {
    Long id;
    Long userId;
    Long factId;
    KnowledgeStatus status;
    Instant nextReviewDate;
    Instant lastReviewDate;
    int totalRepetitionCount;
    int failureCount;
    int successCount;
    int srsStreak;
    int globalStreak;
    BigDecimal easeFactor;
    double difficulty;
    double stability;
}