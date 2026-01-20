// src/main/java/com/saymyname/core/model/quiz/snapshot/HangmanRules.java
package com.saymyname.core.model.quiz.snapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HangmanRules {

    private Integer maxErrors;
    private Boolean normalized; // ex: ignore accents/case
    private Boolean canSolveWholeWord; // autoriser proposition mot complet
    private List<String> alphabet = new ArrayList<>();

    public HangmanRules() {
    }

    private HangmanRules(Builder b) {
        this.maxErrors = b.maxErrors;
        this.normalized = b.normalized;
        this.canSolveWholeWord = b.canSolveWholeWord;
        this.alphabet = b.alphabet != null ? b.alphabet : new ArrayList<>();
        validateInvariants();
    }

    public void validateInvariants() {
        if (maxErrors == null || maxErrors <= 0) {
            throw new IllegalStateException("HangmanRules.maxErrors must be >= 1");
        }
        if (alphabet == null) {
            throw new IllegalStateException("HangmanRules.alphabet cannot be null");
        }
        if (alphabet.stream().anyMatch(Objects::isNull)) {
            throw new IllegalStateException("HangmanRules.alphabet cannot contain null");
        }
    }

    public Integer getMaxErrors() {
        return maxErrors;
    }

    public Boolean getNormalized() {
        return normalized;
    }

    public Boolean getCanSolveWholeWord() {
        return canSolveWholeWord;
    }

    public List<String> getAlphabet() {
        return alphabet;
    }

    public void setMaxErrors(Integer maxErrors) {
        this.maxErrors = maxErrors;
    }

    public void setNormalized(Boolean normalized) {
        this.normalized = normalized;
    }

    public void setCanSolveWholeWord(Boolean canSolveWholeWord) {
        this.canSolveWholeWord = canSolveWholeWord;
    }

    public void setAlphabet(List<String> alphabet) {
        this.alphabet = alphabet;
    }

    public static class Builder {
        private Integer maxErrors;
        private Boolean normalized;
        private Boolean canSolveWholeWord;
        private List<String> alphabet;

        public Builder withMaxErrors(Integer v) {
            this.maxErrors = v;
            return this;
        }

        public Builder withNormalized(Boolean v) {
            this.normalized = v;
            return this;
        }

        public Builder withCanSolveWholeWord(Boolean v) {
            this.canSolveWholeWord = v;
            return this;
        }

        public Builder withAlphabet(List<String> v) {
            this.alphabet = v;
            return this;
        }

        public HangmanRules build() {
            return new HangmanRules(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof HangmanRules))
            return false;
        HangmanRules that = (HangmanRules) o;
        return Objects.equals(maxErrors, that.maxErrors)
                && Objects.equals(normalized, that.normalized)
                && Objects.equals(canSolveWholeWord, that.canSolveWholeWord)
                && Objects.equals(alphabet, that.alphabet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxErrors, normalized, canSolveWholeWord, alphabet);
    }
}
