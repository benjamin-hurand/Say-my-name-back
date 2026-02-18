package com.saymyname.core.model.quiz;

import com.saymyname.core.model.enums.quiz.QuizFollowUpReason;
import com.saymyname.core.model.enums.quiz.QuizFollowUpStrategy;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class QuizFollowUp {
    QuizFollowUpStrategy strategy;
    QuizFollowUpReason reason;
}
