// src/main/java/com/saymyname/core/model/quiz/QuizAnswerResultBase.java
package com.saymyname.core.model.quiz;

import java.util.List;
import java.util.Objects;

public class QuizAnswerResultBase {

    private boolean correct;
    private String feedbackMessage;
    private QuizQuestion nextQuestion; // nullable (training batch)
    private List<QuizAnswerItemResult> itemResults; // nullable/empty ok

    public QuizAnswerResultBase() {
    }

    protected QuizAnswerResultBase(Builder<?> b) {
        this.correct = b.correct;
        this.feedbackMessage = b.feedbackMessage;
        this.nextQuestion = b.nextQuestion;
        this.itemResults = b.itemResults;
    }

    public boolean isCorrect() {
        return correct;
    }

    public String getFeedbackMessage() {
        return feedbackMessage;
    }

    public QuizQuestion getNextQuestion() {
        return nextQuestion;
    }

    public List<QuizAnswerItemResult> getItemResults() {
        return itemResults;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public void setFeedbackMessage(String feedbackMessage) {
        this.feedbackMessage = feedbackMessage;
    }

    public void setNextQuestion(QuizQuestion nextQuestion) {
        this.nextQuestion = nextQuestion;
    }

    public void setItemResults(List<QuizAnswerItemResult> itemResults) {
        this.itemResults = itemResults;
    }

    public static abstract class Builder<T extends Builder<T>> {
        private boolean correct;
        private String feedbackMessage;
        private QuizQuestion nextQuestion;
        private List<QuizAnswerItemResult> itemResults;

        public T withCorrect(boolean v) {
            this.correct = v;
            return self();
        }

        public T withFeedbackMessage(String v) {
            this.feedbackMessage = v;
            return self();
        }

        public T withNextQuestion(QuizQuestion v) {
            this.nextQuestion = v;
            return self();
        }

        public T withItemResults(List<QuizAnswerItemResult> v) {
            this.itemResults = v;
            return self();
        }

        protected abstract T self();

        public abstract QuizAnswerResultBase build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof QuizAnswerResultBase that))
            return false;
        return correct == that.correct
                && Objects.equals(feedbackMessage, that.feedbackMessage)
                && Objects.equals(nextQuestion, that.nextQuestion)
                && Objects.equals(itemResults, that.itemResults);
    }

    @Override
    public int hashCode() {
        return Objects.hash(correct, feedbackMessage, nextQuestion, itemResults);
    }
}
