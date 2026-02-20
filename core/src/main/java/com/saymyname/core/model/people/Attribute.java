// src/main/java/com/saymyname/core/model/people/Attribute.java
package com.saymyname.core.model.people;

import com.saymyname.core.model.enums.CasingStrategy;
import com.saymyname.core.model.enums.ConstraintKind;
import com.saymyname.core.model.enums.EditPolicy;
import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Attribute {

    Long id;

    // "comme avant"
    String name;

    @Builder.Default
    int displayOrder = 100;

    boolean primaryField;
    boolean category;

    int maxValues;
    boolean filter;

    // si tu les avais vraiment avant et que tu les utilises encore
    String minValue;
    String maxValue;

    boolean sort;
    boolean initializable;
    boolean required;

    @Builder.Default
    AttributeType type = AttributeType.TEXT;

    @Builder.Default
    EditPolicy editPolicy = EditPolicy.FREE;

    @Builder.Default
    CasingStrategy casingStrategy = CasingStrategy.NONE;

    @Builder.Default
    ConstraintKind constraintKind = ConstraintKind.NONE;

    // "comme avant"
    Map<String, Object> constraintPayload;
}
