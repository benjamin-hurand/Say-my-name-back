package com.saymyname.core.model.quiz;

import com.saymyname.core.model.enums.quiz.HangmanAction;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class QuizAnswerSubmission {
    String userAnswer;
    Long selectedChoiceId;
    @Builder.Default
    List<Long> selectedChoiceIds = List.of();
    Boolean swipeRight;
    @Builder.Default
    List<Long> orderingIds = List.of();
    @Builder.Default
    List<QuizAssociationPair> pairs = List.of();
    Integer timeMs;
    HangmanSubmission hangman;
    WordPuzzleSubmission wordPuzzle;

    @Value
    @Builder(toBuilder = true)
    public static class HangmanSubmission {
        HangmanAction action;
        String letter;
        String value;
    }

    @Value
    @Builder(toBuilder = true)
    public static class WordPuzzleSubmission {
        String word;
    }
}
