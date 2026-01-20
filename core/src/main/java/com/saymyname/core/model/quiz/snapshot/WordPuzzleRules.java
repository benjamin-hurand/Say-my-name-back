// src/main/java/com/saymyname/core/model/quiz/snapshot/WordPuzzleRules.java
package com.saymyname.core.model.quiz.snapshot;

import java.util.Objects;

/**
 * Immutable rules for WORD_PUZZLE format (Wordle-like).
 * Frozen at question emission time.
 */
public class WordPuzzleRules {

    private Integer maxAttempts;        // Maximum number of word guesses (default: 6)
    private Integer wordLength;         // Solution word length (for validation)
    private Boolean caseSensitive;      // Case sensitivity flag (default: false)
    private Boolean allowRepeatedLetters; // Allow repeated letters in solution (default: true)

    public WordPuzzleRules() {
    }

    private WordPuzzleRules(Builder b) {
        this.maxAttempts = b.maxAttempts;
        this.wordLength = b.wordLength;
        this.caseSensitive = b.caseSensitive;
        this.allowRepeatedLetters = b.allowRepeatedLetters;
        validateInvariants();
    }

    public void validateInvariants() {
        if (maxAttempts == null || maxAttempts < 1) {
            throw new IllegalStateException("WordPuzzleRules.maxAttempts must be >= 1");
        }
        if (wordLength == null || wordLength < 1) {
            throw new IllegalStateException("WordPuzzleRules.wordLength must be >= 1");
        }
    }

    // ---------------- Getters/Setters ----------------

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public Integer getWordLength() {
        return wordLength;
    }

    public Boolean getCaseSensitive() {
        return caseSensitive;
    }

    public Boolean getAllowRepeatedLetters() {
        return allowRepeatedLetters;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public void setWordLength(Integer wordLength) {
        this.wordLength = wordLength;
    }

    public void setCaseSensitive(Boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    public void setAllowRepeatedLetters(Boolean allowRepeatedLetters) {
        this.allowRepeatedLetters = allowRepeatedLetters;
    }

    // ---------------- Builder ----------------

    public static class Builder {
        private Integer maxAttempts;
        private Integer wordLength;
        private Boolean caseSensitive;
        private Boolean allowRepeatedLetters;

        public Builder withMaxAttempts(Integer v) {
            this.maxAttempts = v;
            return this;
        }

        public Builder withWordLength(Integer v) {
            this.wordLength = v;
            return this;
        }

        public Builder withCaseSensitive(Boolean v) {
            this.caseSensitive = v;
            return this;
        }

        public Builder withAllowRepeatedLetters(Boolean v) {
            this.allowRepeatedLetters = v;
            return this;
        }

        public WordPuzzleRules build() {
            return new WordPuzzleRules(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof WordPuzzleRules))
            return false;
        WordPuzzleRules that = (WordPuzzleRules) o;
        return Objects.equals(maxAttempts, that.maxAttempts)
                && Objects.equals(wordLength, that.wordLength)
                && Objects.equals(caseSensitive, that.caseSensitive)
                && Objects.equals(allowRepeatedLetters, that.allowRepeatedLetters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maxAttempts, wordLength, caseSensitive, allowRepeatedLetters);
    }
}
