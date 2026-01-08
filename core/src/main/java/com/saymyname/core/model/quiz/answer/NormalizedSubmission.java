package com.saymyname.core.model.quiz.answer;

public sealed interface NormalizedSubmission permits
        NormalizedText,
        NormalizedChoice,
        NormalizedBinary,
        NormalizedOrdering,
        NormalizedAssociation {

    /**
     * Canonical string for audit/debug/persistence.
     * Must be stable and deterministic.
     */
    String auditString();
}
