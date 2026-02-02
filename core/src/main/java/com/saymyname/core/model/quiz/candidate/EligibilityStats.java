// core/model/quiz/candidate/EligibilityStats.java
package com.saymyname.core.model.quiz.candidate;

/**
 * Lightweight eligibility counts for format planning.
 * Single DB round-trip, no full Person loading.
 *
 * Used by FormatPlanner to determine which formats are feasible
 * given the current candidate pool.
 */
public record EligibilityStats(
        long totalEligible,
        long withApprovedPhoto,
        long withGameModeAttrs) {

    public EligibilityStats {
        if (totalEligible < 0) {
            throw new IllegalArgumentException("totalEligible must be >= 0");
        }
        if (withApprovedPhoto < 0) {
            throw new IllegalArgumentException("withApprovedPhoto must be >= 0");
        }
        if (withGameModeAttrs < 0) {
            throw new IllegalArgumentException("withGameModeAttrs must be >= 0");
        }
    }

    /**
     * Empty stats (no candidates at all).
     */
    public static EligibilityStats empty() {
        return new EligibilityStats(0, 0, 0);
    }

    /**
     * Check if MCQ format is feasible (requires minChoices candidates).
     */
    public boolean canMcq(int minChoices) {
        return totalEligible >= minChoices;
    }

    /**
     * Check if photo-based formats (BINARY_SWIPE) are feasible.
     */
    public boolean canPhotoFormat() {
        return withApprovedPhoto >= 1;
    }

    /**
     * Check if association format is feasible (requires minItems pairs).
     */
    public boolean canAssociation(int minItems) {
        return totalEligible >= minItems;
    }

    /**
     * Check if ordering format is feasible (requires minItems to order).
     */
    public boolean canOrdering(int minItems) {
        return totalEligible >= minItems;
    }

    /**
     * Check if pool is completely empty.
     */
    public boolean isEmpty() {
        return totalEligible == 0;
    }

    /**
     * Check if at least one single-candidate format is feasible.
     */
    public boolean canSingleCandidate() {
        return totalEligible >= 1;
    }
}
