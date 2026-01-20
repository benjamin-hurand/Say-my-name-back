package com.saymyname.core.model.quiz.answer;

public sealed interface NormalizedAudit permits
        NormalizedText,
        NormalizedChoice,
        NormalizedBinary,
        NormalizedOrdering,
        NormalizedAssociation,
        NormalizedHangman,
        NormalizedWordPuzzle {

    /**
     * Canonical string for audit/debug/persistence.
     * Must be stable and deterministic.
     */
    String auditString();
}
