package com.saymyname.core.model.enums;

import java.util.List;

public enum KnowledgeStatus {
    UNKNOWN,
    DISCOVERED,
    LEARNED,
    MASTERED;

    /**
     * Renvoie la “sous-liste” de statuts à utiliser pour
     * countByCourseAndStatus(…, this).
     */
    public List<KnowledgeStatus> scope() {
        return switch (this) {
            case UNKNOWN -> List.of(UNKNOWN);
            case DISCOVERED -> List.of(DISCOVERED, LEARNED, MASTERED);
            case LEARNED -> List.of(LEARNED, MASTERED);
            case MASTERED -> List.of(MASTERED);
        };
    }
}
