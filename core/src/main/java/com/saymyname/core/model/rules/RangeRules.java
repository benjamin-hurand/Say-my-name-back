package com.saymyname.core.model.rules;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class RangeRules {
    String min;
    String max;
    @Builder.Default
    Boolean inclusive = Boolean.TRUE;
    Integer step;
}
