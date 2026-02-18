package com.saymyname.core.model.people;

import com.saymyname.core.model.enums.CasingStrategy;
import com.saymyname.core.model.enums.ConstraintKind;
import com.saymyname.core.model.enums.EditPolicy;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Attribute {
    Long id;
    String attributeName;
    int displayOrder;
    boolean primaryField;
    boolean category;
    int maxValues;
    boolean filter;
    boolean sort;
    boolean initializable;
    boolean required;
    AttributeType type;
    EditPolicy editPolicy;
    CasingStrategy casingStrategy;
    ConstraintKind constraintKind;
    String constraintPayload;
}