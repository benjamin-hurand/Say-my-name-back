package com.saymyname.core.model.quiz;

import com.saymyname.core.model.course.ResultAttribute;
import com.saymyname.core.model.enums.course.QuizQuestionItemRole;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class QuizAnswerItemResult {
    int position;
    QuizQuestionItemRole role;
    Long knowledgeId;
    Long personId;
    boolean correct;
    String userAnswerNormalized;
    String correctAnswer;
    @Builder.Default
    List<ResultAttribute> resultAttributes = List.of();
}
