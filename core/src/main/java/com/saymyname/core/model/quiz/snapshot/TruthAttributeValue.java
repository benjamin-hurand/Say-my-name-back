package com.saymyname.core.model.quiz.snapshot;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class TruthAttributeValue {
    Long attributeId;
    String value;

    public void validateInvariants() {
        if (attributeId == null) {
            throw new IllegalStateException("TruthAttributeValue.attributeId is required");
        }
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("TruthAttributeValue.value is required");
        }
    }
}
