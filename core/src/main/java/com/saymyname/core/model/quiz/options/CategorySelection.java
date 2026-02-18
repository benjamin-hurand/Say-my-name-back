package com.saymyname.core.model.quiz.options;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class CategorySelection {
    Long attributeId;
    String value;
}
