package com.saymyname.core.model.people;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class AttributeEnumOption {
    Long id;
    Long attributeId;
    String code;
    String label;
    int orderIndex;
    boolean active;
}