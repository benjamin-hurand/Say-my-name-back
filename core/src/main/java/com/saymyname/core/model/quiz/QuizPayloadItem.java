package com.saymyname.core.model.quiz;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import lombok.experimental.SuperBuilder;

@Value
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class QuizPayloadItem extends QuizDisplayItem {
    public String getLabelId() {
        return getLabel();
    }
}
