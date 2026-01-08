// src/main/java/com/saymyname/core/model/quiz/QuizFollowUp.java
package com.saymyname.core.model.quiz;

import java.util.Objects;

import com.saymyname.core.model.enums.quiz.QuizFollowUpReason;
import com.saymyname.core.model.enums.quiz.QuizFollowUpStrategy;

public class QuizFollowUp {

    private QuizFollowUpStrategy strategy;
    private QuizFollowUpReason reason;

    public QuizFollowUp() {
    }

    public QuizFollowUp(QuizFollowUpStrategy strategy, QuizFollowUpReason reason) {
        this.strategy = strategy;
        this.reason = reason;
    }

    public QuizFollowUpStrategy getStrategy() {
        return strategy;
    }

    public QuizFollowUpReason getReason() {
        return reason;
    }

    public void setStrategy(QuizFollowUpStrategy strategy) {
        this.strategy = strategy;
    }

    public void setReason(QuizFollowUpReason reason) {
        this.reason = reason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof QuizFollowUp))
            return false;
        QuizFollowUp that = (QuizFollowUp) o;
        return strategy == that.strategy && reason == that.reason;
    }

    @Override
    public int hashCode() {
        return Objects.hash(strategy, reason);
    }
}
