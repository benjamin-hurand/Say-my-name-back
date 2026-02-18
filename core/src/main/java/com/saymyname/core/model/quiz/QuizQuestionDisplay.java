package com.saymyname.core.model.quiz;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class QuizQuestionDisplay {
    String prompt;
    String subtitle;
    String inputPlaceholder;
    Boolean timed;
    Integer timeLimitMs;
}
