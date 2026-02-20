package com.saymyname.core.model.quiz.answer;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class NormalizedChoice implements NormalizedAudit {
    Long selectedChoiceId;
    String selectedValue;

    @Override
    public String auditString() {
        if (selectedChoiceId == null) {
            return null;
        }
        return selectedChoiceId + ":" + (selectedValue == null ? "" : selectedValue);
    }

    // Backward-compatible record-style accessors.
    public Long selectedChoiceId() {
        return selectedChoiceId;
    }

    public String selectedValue() {
        return selectedValue;
    }
}
