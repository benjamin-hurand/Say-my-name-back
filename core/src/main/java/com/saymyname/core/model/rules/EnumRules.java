package com.saymyname.core.model.rules;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder(toBuilder = true)
@Jacksonized
public class EnumRules {
    @Builder.Default
    Boolean allowInactive = Boolean.FALSE;
    @Builder.Default
    Boolean storeCode = Boolean.TRUE;
}
