package com.saymyname.core.model.quiz;

import com.saymyname.core.model.enums.quiz.QuizOrderingRule;
import com.saymyname.core.model.enums.quiz.QuizPayloadType;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class QuizQuestionPayload {
    QuizPayloadType type;
    String mask;
    Integer maxErrors;
    Integer wordLength;
    Integer maxAttempts;
    @Builder.Default
    List<QuizChoice> choices = List.of();
    Boolean allowMultiple;
    QuizChoice proposition;
    @Builder.Default
    List<QuizPayloadItem> items = List.of();
    QuizOrderingRule orderBy;
    QuizLayoutHints layoutHints;
}
