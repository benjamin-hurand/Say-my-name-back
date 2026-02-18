package com.saymyname.core.model.quiz;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class QuizEntry {
    Long personId;
    String storageKey;
    String initials;
}
