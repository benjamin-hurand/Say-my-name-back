package com.saymyname.core.model.course;

import com.saymyname.core.model.people.Attribute;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ResultAttribute {
    Attribute attribute;
    String value;
    boolean correct;
    boolean target;
}
