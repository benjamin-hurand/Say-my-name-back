// src/main/java/com/saymyname/core/model/quiz/snapshot/QuizTruthRules.java
package com.saymyname.core.model.quiz.snapshot;

import java.util.Objects;

public class QuizTruthRules {

    /**
     * Exemple: "AND", "OR" si tu veux garder trace du gameMode operator,
     * ou un profil de matching textuel.
     */
    private String operator;

    /** Text matching flags (si utile) */
    private boolean ignoreCase = true;
    private boolean ignoreDiacritics = true;
    private boolean trim = true;

    public QuizTruthRules() {
    }

    private QuizTruthRules(Builder b) {
        this.operator = b.operator;
        this.ignoreCase = b.ignoreCase;
        this.ignoreDiacritics = b.ignoreDiacritics;
        this.trim = b.trim;
        validateInvariants();
    }

    public void validateInvariants() {
        // operator peut être null si format non-textuel
    }

    public String getOperator() {
        return operator;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public boolean isIgnoreDiacritics() {
        return ignoreDiacritics;
    }

    public boolean isTrim() {
        return trim;
    }

    public static class Builder {
        private String operator;
        private boolean ignoreCase = true;
        private boolean ignoreDiacritics = true;
        private boolean trim = true;

        public Builder withOperator(String v) {
            this.operator = v;
            return this;
        }

        public Builder withIgnoreCase(boolean v) {
            this.ignoreCase = v;
            return this;
        }

        public Builder withIgnoreDiacritics(boolean v) {
            this.ignoreDiacritics = v;
            return this;
        }

        public Builder withTrim(boolean v) {
            this.trim = v;
            return this;
        }

        public QuizTruthRules build() {
            return new QuizTruthRules(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof QuizTruthRules))
            return false;
        QuizTruthRules that = (QuizTruthRules) o;
        return ignoreCase == that.ignoreCase
                && ignoreDiacritics == that.ignoreDiacritics
                && trim == that.trim
                && Objects.equals(operator, that.operator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(operator, ignoreCase, ignoreDiacritics, trim);
    }
}
