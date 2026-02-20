package com.saymyname.core.model.quiz.candidate;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class EligibilityStats {
    long totalEligible;
    long approvedPhoto;
    long targetAttrs;

    public static EligibilityStats empty() {
        return EligibilityStats.builder().totalEligible(0).approvedPhoto(0).targetAttrs(0).build();
    }

    public boolean canMcq(int minChoices) {
        return totalEligible >= minChoices;
    }

    public boolean canPhotoFormat() {
        return approvedPhoto >= 1;
    }

    public boolean canAssociation(int minItems) {
        return totalEligible >= minItems;
    }

    public boolean canOrdering(int minItems) {
        return totalEligible >= minItems;
    }

    public boolean isEmpty() {
        return totalEligible == 0;
    }

    public boolean canSingleCandidate() {
        return totalEligible >= 1;
    }

    // Backward-compatible record-style accessors.
    public long totalEligible() {
        return totalEligible;
    }

    public long approvedPhoto() {
        return approvedPhoto;
    }

    public long targetAttrs() {
        return targetAttrs;
    }
}
