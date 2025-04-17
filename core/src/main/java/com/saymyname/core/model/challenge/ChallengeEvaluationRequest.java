// src/main/java/com/saymyname/core/model/challenge/ChallengeEvaluationRequest.java
package com.saymyname.core.model.challenge;

import java.util.List;
import java.util.Objects;

public class ChallengeEvaluationRequest {
    private List<ChallengeHistoryEntry> history;

    // Constructeur par défaut
    public ChallengeEvaluationRequest() {
    }

    private ChallengeEvaluationRequest(Builder builder) {
        this.history = builder.history;
    }

    // Getter
    public List<ChallengeHistoryEntry> getHistory() {
        return history;
    }

    // Setter
    public void setHistory(List<ChallengeHistoryEntry> history) {
        this.history = history;
    }

    public static class Builder {
        private List<ChallengeHistoryEntry> history;

        public Builder withHistory(List<ChallengeHistoryEntry> history) {
            this.history = history;
            return this;
        }

        public ChallengeEvaluationRequest build() {
            return new ChallengeEvaluationRequest(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ChallengeEvaluationRequest that = (ChallengeEvaluationRequest) o;
        return Objects.equals(history, that.history);
    }

    @Override
    public int hashCode() {
        return Objects.hash(history);
    }

    @Override
    public String toString() {
        return "ChallengeEvaluationRequest{" +
                "history=" + history +
                '}';
    }
}
