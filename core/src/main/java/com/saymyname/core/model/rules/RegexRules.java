package com.saymyname.core.model.rules;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class RegexRules {
    String pattern;
    Integer minLength;
    Integer maxLength;
    Boolean caseInsensitive;
}
