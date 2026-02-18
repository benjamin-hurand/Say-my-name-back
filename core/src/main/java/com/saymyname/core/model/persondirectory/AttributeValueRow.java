package com.saymyname.core.model.persondirectory;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class AttributeValueRow {
    Long personId;
    Long attributeId;
    String value;
    Integer displayOrder;
    Boolean primaryField;
}
