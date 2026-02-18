package com.saymyname.core.model.course;

import com.saymyname.core.model.enums.quiz.QuizDecisionReasonCode;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class CourseQuestionPlan {
    QuizFormat format;
    boolean timed;
    Integer timeLimitMs;
    int targetCount;
    @Builder.Default
    List<Long> targetKnowledgeIds = List.of();
    String paramsJson;
    QuizDecisionReasonCode reasonCode;
    String reasonDetailsJson;

    public boolean hasValidTimedConfig() {
        return !timed || (timeLimitMs != null && timeLimitMs >= 1000);
    }
}
