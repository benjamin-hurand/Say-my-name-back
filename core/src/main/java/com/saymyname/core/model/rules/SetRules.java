package com.saymyname.core.model.rules;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class SetRules {
    @Builder.Default
    List<String> values = List.of();
    @Builder.Default
    Boolean strict = Boolean.TRUE;
}
