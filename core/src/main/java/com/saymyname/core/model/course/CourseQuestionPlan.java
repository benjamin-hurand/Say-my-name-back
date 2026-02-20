package com.saymyname.core.model.course;

import com.saymyname.core.model.enums.quiz.QuizDecisionReasonCode;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import java.util.List;
import java.util.Objects;
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

    public void validateInvariants() {
        if (format == null)
            throw new IllegalStateException("CourseQuestionPlan.format is required");
        if (targetCount < 1)
            throw new IllegalStateException("CourseQuestionPlan.targetCount must be >= 1");

        if (timed && (timeLimitMs == null || timeLimitMs < 1000)) {
            throw new IllegalStateException("CourseQuestionPlan.timeLimitMs must be >= 1000 when timed=true");
        }

        if (targetKnowledgeIds == null || targetKnowledgeIds.isEmpty()) {
            throw new IllegalStateException("CourseQuestionPlan.targetKnowledgeIds must contain at least 1 id");
        }
        if (targetKnowledgeIds.stream().anyMatch(Objects::isNull)) {
            throw new IllegalStateException("CourseQuestionPlan.targetKnowledgeIds cannot contain null");
        }
        if (targetKnowledgeIds.size() != targetCount) {
            throw new IllegalStateException("CourseQuestionPlan.targetCount must match targetKnowledgeIds size");
        }
        if (reasonCode == null) {
            throw new IllegalStateException("CourseQuestionPlan.reasonCode is required");
        }
    }
}
