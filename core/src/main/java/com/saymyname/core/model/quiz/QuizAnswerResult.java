package com.saymyname.core.model.quiz;

import java.util.List;
import java.util.Objects;

import com.saymyname.core.model.leaderboard.XpAward;
import com.saymyname.core.model.quiz.snapshot.MultiStepState;

public class QuizAnswerResult {

    private boolean correct;
    private String feedbackMessage;

    private QuizQuestion nextQuestion; // nullable
    private List<QuizAnswerItemResult> itemResults; // nullable/empty ok

    // Multi-step formats support
    private Boolean isComplete; // null=single-shot, true/false=multi-step
    private MultiStepState currentState; // HangmanSnapshotState or WordPuzzleSnapshotState

    // XP award (optional)
    private XpAward xpAward; // nullable (or XpAward.none())

    public QuizAnswerResult() {
    }

    private QuizAnswerResult(Builder b) {
        this.correct = b.correct;
        this.feedbackMessage = b.feedbackMessage;
        this.nextQuestion = b.nextQuestion;
        this.itemResults = b.itemResults;
        this.isComplete = b.isComplete;
        this.currentState = b.currentState;
        this.xpAward = b.xpAward;
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

    public Boolean getIsComplete() {
        return isComplete;
    }

    public MultiStepState getCurrentState() {
        return currentState;
    }

    public XpAward getXpAward() {
        return xpAward;
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

    public void setIsComplete(Boolean isComplete) {
        this.isComplete = isComplete;
    }

    public void setCurrentState(MultiStepState currentState) {
        this.currentState = currentState;
    }

    public void setXpAward(XpAward xpAward) {
        this.xpAward = xpAward;
    }

    // ---------------- Builder ----------------

    public static class Builder {
        private boolean correct;
        private String feedbackMessage;
        private QuizQuestion nextQuestion;
        private List<QuizAnswerItemResult> itemResults;
        private Boolean isComplete;
        private MultiStepState currentState;
        private XpAward xpAward;

        public Builder from(QuizAnswerResult other) {
            if (other != null) {
                this.correct = other.correct;
                this.feedbackMessage = other.feedbackMessage;
                this.nextQuestion = other.nextQuestion;
                this.itemResults = other.itemResults;
                this.isComplete = other.isComplete;
                this.currentState = other.currentState;
                this.xpAward = other.xpAward;
            }
            return this;
        }

        public Builder withCorrect(boolean v) {
            this.correct = v;
            return this;
        }

        public Builder withFeedbackMessage(String v) {
            this.feedbackMessage = v;
            return this;
        }

        public Builder withNextQuestion(QuizQuestion v) {
            this.nextQuestion = v;
            return this;
        }

        public Builder withItemResults(List<QuizAnswerItemResult> v) {
            this.itemResults = v;
            return this;
        }

        public Builder withIsComplete(Boolean v) {
            this.isComplete = v;
            return this;
        }

        public Builder withCurrentState(MultiStepState v) {
            this.currentState = v;
            return this;
        }

        public Builder withXpAward(XpAward v) {
            this.xpAward = v;
            return this;
        }

        public QuizAnswerResult build() {
            return new QuizAnswerResult(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof QuizAnswerResult that))
            return false;
        return correct == that.correct
                && Objects.equals(feedbackMessage, that.feedbackMessage)
                && Objects.equals(nextQuestion, that.nextQuestion)
                && Objects.equals(itemResults, that.itemResults)
                && Objects.equals(isComplete, that.isComplete)
                && Objects.equals(currentState, that.currentState)
                && Objects.equals(xpAward, that.xpAward);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                correct, feedbackMessage, nextQuestion, itemResults,
                isComplete, currentState, xpAward);
    }

    @Override
    public String toString() {
        return "QuizAnswerResult{" +
                "correct=" + correct +
                ", feedbackMessage='" + feedbackMessage + '\'' +
                ", nextQuestion=" + nextQuestion +
                ", itemResults=" + itemResults +
                ", isComplete=" + isComplete +
                ", currentState=" + currentState +
                ", xpAward=" + xpAward +
                '}';
    }
}
