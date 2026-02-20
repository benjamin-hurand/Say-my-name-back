package com.saymyname.core.model.quiz.answer;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class NormalizedBinary implements NormalizedAudit {
    Boolean swipeRight;

    @Override
    public String auditString() {
        if (swipeRight == null) {
            return null;
        }
        return swipeRight ? "true" : "false";
    }

    // Backward-compatible record-style accessor.
    public Boolean swipeRight() {
        return swipeRight;
    }
}
