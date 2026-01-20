// src/main/java/com/saymyname/core/model/quiz/snapshot/WordPuzzleSnapshotState.java
package com.saymyname.core.model.quiz.snapshot;

import com.saymyname.core.model.enums.quiz.LetterFeedback;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Mutable state for WORD_PUZZLE format.
 * Tracks attempt attempt and completion status.
 */
public class WordPuzzleSnapshotState implements MultiStepState {

    /**
     * Single attempt entry with word guess and per-letter feedback.
     */
    public static class AttemptEntry {
        private String word; // User's guess (canonicalized)
        private List<LetterFeedback> feedback; // Feedback per letter position
        private int position; // Attempt number (0-indexed)

        public AttemptEntry() {
        }

        private AttemptEntry(Builder b) {
            this.word = b.word;
            this.feedback = b.feedback != null ? b.feedback : new ArrayList<>();
            this.position = b.position;
            validateInvariants();
        }

        public void validateInvariants() {
            if (word == null || word.isBlank()) {
                throw new IllegalStateException("AttemptEntry.word is required");
            }
            if (feedback == null) {
                throw new IllegalStateException("AttemptEntry.feedback cannot be null");
            }
            if (feedback.stream().anyMatch(Objects::isNull)) {
                throw new IllegalStateException("AttemptEntry.feedback cannot contain null");
            }
            if (feedback.size() != word.length()) {
                throw new IllegalStateException(
                        "AttemptEntry.feedback size must match word length: " + word.length());
            }
            if (position < 0) {
                throw new IllegalStateException("AttemptEntry.position must be >= 0");
            }
        }

        public String getWord() {
            return word;
        }

        public List<LetterFeedback> getFeedback() {
            return feedback;
        }

        public int getPosition() {
            return position;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public void setFeedback(List<LetterFeedback> feedback) {
            this.feedback = feedback;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public static class Builder {
            private String word;
            private List<LetterFeedback> feedback;
            private int position;

            public Builder withWord(String v) {
                this.word = v;
                return this;
            }

            public Builder withFeedback(List<LetterFeedback> v) {
                this.feedback = v;
                return this;
            }

            public Builder withPosition(int v) {
                this.position = v;
                return this;
            }

            public AttemptEntry build() {
                return new AttemptEntry(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof AttemptEntry))
                return false;
            AttemptEntry that = (AttemptEntry) o;
            return position == that.position
                    && Objects.equals(word, that.word)
                    && Objects.equals(feedback, that.feedback);
        }

        @Override
        public int hashCode() {
            return Objects.hash(word, feedback, position);
        }
    }

    private List<AttemptEntry> attempts = new ArrayList<>(); // Attempt of all attempts
    private Integer attemptsRemaining; // Remaining attempts
    private Boolean solved; // True if solution found
    private Boolean failed; // True if max attempts reached without solving

    public WordPuzzleSnapshotState() {
    }

    private WordPuzzleSnapshotState(Builder b) {
        this.attempts = b.attempts != null ? b.attempts : new ArrayList<>();
        this.attemptsRemaining = b.attemptsRemaining;
        this.solved = b.solved;
        this.failed = b.failed;
        validateInvariants();
    }

    @Override
    public void validateInvariants() {
        if (attempts == null) {
            throw new IllegalStateException("WordPuzzleSnapshotState.attempts cannot be null");
        }
        if (attempts.stream().anyMatch(Objects::isNull)) {
            throw new IllegalStateException("WordPuzzleSnapshotState.attempts cannot contain null");
        }
        if (attemptsRemaining == null || attemptsRemaining < 0) {
            throw new IllegalStateException("WordPuzzleSnapshotState.attemptsRemaining must be >= 0");
        }
        if (Boolean.TRUE.equals(solved) && Boolean.TRUE.equals(failed)) {
            throw new IllegalStateException("WordPuzzleSnapshotState.solved and failed are mutually exclusive");
        }

        // Validate each attempt entry
        for (AttemptEntry entry : attempts) {
            entry.validateInvariants();
        }
    }

    // ---------------- Getters/Setters ----------------

    public List<AttemptEntry> getAttempts() {
        return attempts;
    }

    public Integer getAttemptsRemaining() {
        return attemptsRemaining;
    }

    public Boolean getSolved() {
        return solved;
    }

    public Boolean getFailed() {
        return failed;
    }

    public void setAttempts(List<AttemptEntry> attempts) {
        this.attempts = attempts;
    }

    public void setAttemptsRemaining(Integer attemptsRemaining) {
        this.attemptsRemaining = attemptsRemaining;
    }

    public void setSolved(Boolean solved) {
        this.solved = solved;
    }

    public void setFailed(Boolean failed) {
        this.failed = failed;
    }

    // ---------------- Builder ----------------

    public static class Builder {
        private List<AttemptEntry> attempts;
        private Integer attemptsRemaining;
        private Boolean solved;
        private Boolean failed;

        public Builder withAttempts(List<AttemptEntry> v) {
            this.attempts = v;
            return this;
        }

        public Builder withAttemptsRemaining(Integer v) {
            this.attemptsRemaining = v;
            return this;
        }

        public Builder withSolved(Boolean v) {
            this.solved = v;
            return this;
        }

        public Builder withFailed(Boolean v) {
            this.failed = v;
            return this;
        }

        public WordPuzzleSnapshotState build() {
            return new WordPuzzleSnapshotState(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof WordPuzzleSnapshotState))
            return false;
        WordPuzzleSnapshotState that = (WordPuzzleSnapshotState) o;
        return Objects.equals(attempts, that.attempts)
                && Objects.equals(attemptsRemaining, that.attemptsRemaining)
                && Objects.equals(solved, that.solved)
                && Objects.equals(failed, that.failed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attempts, attemptsRemaining, solved, failed);
    }
}
