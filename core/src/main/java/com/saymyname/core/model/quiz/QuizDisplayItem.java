package com.saymyname.core.model.quiz;

import lombok.Value;
import lombok.experimental.NonFinal;
import lombok.experimental.SuperBuilder;

@Value
@SuperBuilder(toBuilder = true)
@NonFinal
public class QuizDisplayItem {
    Long id;
    String label;
    String value;
    String storageKey;
    Long personId;
    String role;
}
