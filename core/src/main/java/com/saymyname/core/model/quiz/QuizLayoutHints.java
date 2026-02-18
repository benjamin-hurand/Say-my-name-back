package com.saymyname.core.model.quiz;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class QuizLayoutHints {
    String layout;
    String itemStyle;
    String photoAspect;
    String photoSize;
}
