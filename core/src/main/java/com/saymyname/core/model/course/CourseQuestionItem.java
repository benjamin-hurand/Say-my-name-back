package com.saymyname.core.model.course;

import com.saymyname.core.model.enums.course.QuizQuestionItemRole;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class CourseQuestionItem {
    Long id;
    Long attemptId;
    int position;
    QuizQuestionItemRole role;
    Long knowledgeId;
    Long personId;
    boolean answered;
    Boolean correct;
    String normalizedAnswer;
}