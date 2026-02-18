package com.saymyname.core.model.quiz;

import com.saymyname.core.model.enums.quiz.QuizDecisionReasonCode;
import com.saymyname.core.model.enums.quiz.QuizQuestionSource;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class QuizQuestionSpec {
    QuizQuestionSource source;
    Long personId;
    String storageKey;
    Long gameModeId;
    @Builder.Default
    List<Long> targetAttributeIds = List.of();
    String operator;
    QuizQuestionContext context;
    String initials;
    @Builder.Default
    List<Long> candidatePoolPersonIds = List.of();
    @Builder.Default
    List<QuizPayloadItem> candidatePoolItems = List.of();
    Boolean timed;
    Integer timeLimitMs;
    Integer maxErrorsOverride;
    QuizDecisionReasonCode reasonCode;
    String reasonDetailsJson;
}
