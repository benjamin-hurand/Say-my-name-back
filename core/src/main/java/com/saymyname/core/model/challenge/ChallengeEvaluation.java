// src/main/java/com/saymyname/core/model/challenge/ChallengeEvaluation.java
package com.saymyname.core.model.challenge;

import java.util.List;
import java.util.Objects;

public class ChallengeEvaluation {
    private Integer totalCorrect;
    private List<CorrectionEntry> entries;

    // Constructeur par défaut
    public ChallengeEvaluation() {
    }

    private ChallengeEvaluation(Builder builder) {
        this.totalCorrect = builder.totalCorrect;
        this.entries = builder.entries;
    }

    // Getters
    public Integer getTotalCorrect() {
        return totalCorrect;
    }

    public List<CorrectionEntry> getEntries() {
        return entries;
    }

    // Setters
    public void setTotalCorrect(Integer totalCorrect) {
        this.totalCorrect = totalCorrect;
    }

    public void setEntries(List<CorrectionEntry> entries) {
        this.entries = entries;
    }

    public static class Builder {
        private Integer totalCorrect;
        private List<CorrectionEntry> entries;

        public Builder withTotalCorrect(Integer totalCorrect) {
            this.totalCorrect = totalCorrect;
            return this;
        }

        public Builder withEntries(List<CorrectionEntry> entries) {
            this.entries = entries;
            return this;
        }

        public ChallengeEvaluation build() {
            return new ChallengeEvaluation(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ChallengeEvaluation that = (ChallengeEvaluation) o;
        return Objects.equals(totalCorrect, that.totalCorrect) &&
                Objects.equals(entries, that.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(totalCorrect, entries);
    }

    @Override
    public String toString() {
        return "ChallengeEvaluation{" +
                "totalCorrect=" + totalCorrect +
                ", entries=" + entries +
                '}';
    }
}
