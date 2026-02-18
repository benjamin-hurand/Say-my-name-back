package com.saymyname.core.model.quiz;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class QuizQuestionHints {
    String initials;
}
