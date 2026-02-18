package com.saymyname.core.model.quiz;

import com.saymyname.core.model.leaderboard.XpAward;
import com.saymyname.core.model.quiz.snapshot.MultiStepState;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class QuizAnswerResult {
    boolean correct;
    String feedbackMessage;
    QuizQuestion nextQuestion;
    @Builder.Default
    List<QuizAnswerItemResult> itemResults = List.of();
    Boolean isComplete;
    MultiStepState currentState;
    XpAward xpAward;
}
