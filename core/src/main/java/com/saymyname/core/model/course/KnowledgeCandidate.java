package com.saymyname.core.model.course;

import com.saymyname.core.model.enums.KnowledgeStatus;
import java.time.LocalDateTime;

public record KnowledgeCandidate(
        Long knowledgeId,
        Long factId,
        Long personId,
        Long attributeId,
        KnowledgeStatus status,
        LocalDateTime nextReviewDate) {
}