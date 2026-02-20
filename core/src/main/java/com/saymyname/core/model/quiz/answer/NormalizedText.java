package com.saymyname.core.model.quiz.answer;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class NormalizedText implements NormalizedAudit {
    String raw;
    String canonical;

    @Override
    public String auditString() {
        return canonical;
    }

    // Backward-compatible record-style accessors.
    public String raw() {
        return raw;
    }

    public String canonical() {
        return canonical;
    }
}
