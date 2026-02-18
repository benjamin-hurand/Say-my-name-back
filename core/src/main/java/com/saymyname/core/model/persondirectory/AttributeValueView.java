package com.saymyname.core.model.persondirectory;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class AttributeValueView {
    Long attributeId;
    String value;
    Integer displayOrder;
    Boolean primaryField;
}
