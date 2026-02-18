package com.saymyname.core.model.quiz;

import com.saymyname.core.model.enums.quiz.QuizDecisionReasonCode;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.quiz.snapshot.MultiStepState;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class QuizQuestion {
    String questionHandle;
    Long personId;
    String storageKey;
    Long gameModeId;
    @Builder.Default
    List<Long> targetAttributeIds = List.of();
    String operator;
    QuizQuestionContext context;
    QuizFormat format;
    QuizQuestionPayload payload;
    QuizQuestionHints hints;
    QuizQuestionDisplay display;
    QuizFollowUp followUp;
    QuizDecisionReasonCode reasonCode;
    String reasonDetailsJson;
    MultiStepState multiStepState;
}
