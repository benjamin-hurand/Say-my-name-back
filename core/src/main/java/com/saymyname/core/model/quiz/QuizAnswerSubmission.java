// src/main/java/com/saymyname/core/model/quiz/QuizAnswerSubmission.java
package com.saymyname.core.model.quiz;

import java.util.List;
import java.util.Objects;

import com.saymyname.core.model.enums.quiz.HangmanAction;

public class QuizAnswerSubmission {

    private String userAnswer; // TEXT_INPUT / CLOZE (et potentiellement legacy HANGMAN si tu veux)
    private Long selectedChoiceId; // MCQ (simple)
    private List<Long> selectedChoiceIds; // MCQ (multiple)
    private Boolean swipeRight; // BINARY_SWIPE
    private List<Long> orderingIds; // ORDERING
    private List<QuizAssociationPair> pairs; // ASSOCIATION
    private Integer timeMs; // optional timing info

    /** ✅ Nouveau : soumission Hangman (lettre par lettre / solve) */
    private HangmanSubmission hangman;

    /** ✅ Nouveau : soumission Word Puzzle (mot complet avec feedback positionnel) */
    private WordPuzzleSubmission wordPuzzle;

    public QuizAnswerSubmission() {
    }

    private QuizAnswerSubmission(Builder b) {
        this.userAnswer = b.userAnswer;
        this.selectedChoiceId = b.selectedChoiceId;
        this.selectedChoiceIds = b.selectedChoiceIds;
        this.swipeRight = b.swipeRight;
        this.orderingIds = b.orderingIds;
        this.pairs = b.pairs;
        this.timeMs = b.timeMs;
        this.hangman = b.hangman;
        this.wordPuzzle = b.wordPuzzle;
    }

    // --------------------
    // Getters
    // --------------------

    public String getUserAnswer() {
        return userAnswer;
    }

    public Long getSelectedChoiceId() {
        return selectedChoiceId;
    }

    public List<Long> getSelectedChoiceIds() {
        return selectedChoiceIds;
    }

    public Boolean getSwipeRight() {
        return swipeRight;
    }

    public List<Long> getOrderingIds() {
        return orderingIds;
    }

    public List<QuizAssociationPair> getPairs() {
        return pairs;
    }

    public Integer getTimeMs() {
        return timeMs;
    }

    public HangmanSubmission getHangman() {
        return hangman;
    }

    public WordPuzzleSubmission getWordPuzzle() {
        return wordPuzzle;
    }

    // --------------------
    // Setters
    // --------------------

    public void setUserAnswer(String userAnswer) {
        this.userAnswer = userAnswer;
    }

    public void setSelectedChoiceId(Long selectedChoiceId) {
        this.selectedChoiceId = selectedChoiceId;
    }

    public void setSelectedChoiceIds(List<Long> selectedChoiceIds) {
        this.selectedChoiceIds = selectedChoiceIds;
    }

    public void setSwipeRight(Boolean swipeRight) {
        this.swipeRight = swipeRight;
    }

    public void setOrderingIds(List<Long> orderingIds) {
        this.orderingIds = orderingIds;
    }

    public void setPairs(List<QuizAssociationPair> pairs) {
        this.pairs = pairs;
    }

    public void setTimeMs(Integer timeMs) {
        this.timeMs = timeMs;
    }

    public void setHangman(HangmanSubmission hangman) {
        this.hangman = hangman;
    }

    public void setWordPuzzle(WordPuzzleSubmission wordPuzzle) {
        this.wordPuzzle = wordPuzzle;
    }

    // --------------------
    // Builder
    // --------------------

    public static class Builder {
        private String userAnswer;
        private Long selectedChoiceId;
        private List<Long> selectedChoiceIds;
        private Boolean swipeRight;
        private List<Long> orderingIds;
        private List<QuizAssociationPair> pairs;
        private Integer timeMs;

        private HangmanSubmission hangman;
        private WordPuzzleSubmission wordPuzzle;

        public Builder withUserAnswer(String v) {
            this.userAnswer = v;
            return this;
        }

        public Builder withSelectedChoiceId(Long v) {
            this.selectedChoiceId = v;
            return this;
        }

        public Builder withSelectedChoiceIds(List<Long> v) {
            this.selectedChoiceIds = v;
            return this;
        }

        public Builder withSwipeRight(Boolean v) {
            this.swipeRight = v;
            return this;
        }

        public Builder withOrderingIds(List<Long> v) {
            this.orderingIds = v;
            return this;
        }

        public Builder withPairs(List<QuizAssociationPair> v) {
            this.pairs = v;
            return this;
        }

        public Builder withTimeMs(Integer v) {
            this.timeMs = v;
            return this;
        }

        /** ✅ Nouveau */
        public Builder withHangman(HangmanSubmission v) {
            this.hangman = v;
            return this;
        }

        /** ✅ Nouveau */
        public Builder withWordPuzzle(WordPuzzleSubmission v) {
            this.wordPuzzle = v;
            return this;
        }

        public QuizAnswerSubmission build() {
            return new QuizAnswerSubmission(this);
        }
    }

    // --------------------
    // HangmanSubmission
    // --------------------

    /**
     * Représente une action Hangman.
     * - GUESS : propose une lettre (champ letter requis)
     * - SOLVE : propose le mot complet (champ value requis)
     */
    public static class HangmanSubmission {

        private HangmanAction action;
        private String letter;
        private String value;

        public HangmanSubmission() {
        }

        private HangmanSubmission(HangmanSubmission.Builder b) {
            this.action = b.action;
            this.letter = b.letter;
            this.value = b.value;
        }

        public HangmanAction getAction() {
            return action;
        }

        public String getLetter() {
            return letter;
        }

        public String getValue() {
            return value;
        }

        public void setAction(HangmanAction action) {
            this.action = action;
        }

        public void setLetter(String letter) {
            this.letter = letter;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public static class Builder {
            private HangmanAction action;
            private String letter;
            private String value;

            public Builder withAction(HangmanAction v) {
                this.action = v;
                return this;
            }

            public Builder withLetter(String v) {
                this.letter = v;
                return this;
            }

            public Builder withValue(String v) {
                this.value = v;
                return this;
            }

            public HangmanSubmission build() {
                return new HangmanSubmission(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof HangmanSubmission))
                return false;
            HangmanSubmission that = (HangmanSubmission) o;
            return action == that.action
                    && Objects.equals(letter, that.letter)
                    && Objects.equals(value, that.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(action, letter, value);
        }

        @Override
        public String toString() {
            return "HangmanSubmission{" +
                    "action=" + action +
                    ", letter='" + letter + '\'' +
                    ", value='" + value + '\'' +
                    '}';
        }
    }

    // --------------------
    // WordPuzzleSubmission
    // --------------------

    /**
     * Représente une soumission Word Puzzle (Wordle-like).
     * L'utilisateur propose un mot complet (pas lettre par lettre).
     */
    public static class WordPuzzleSubmission {

        private String word;  // Mot complet proposé (requis, longueur validée par plugin)

        public WordPuzzleSubmission() {
        }

        private WordPuzzleSubmission(Builder b) {
            this.word = b.word;
        }

        public String getWord() {
            return word;
        }

        public void setWord(String word) {
            this.word = word;
        }

        public static class Builder {
            private String word;

            public Builder withWord(String v) {
                this.word = v;
                return this;
            }

            public WordPuzzleSubmission build() {
                return new WordPuzzleSubmission(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof WordPuzzleSubmission))
                return false;
            WordPuzzleSubmission that = (WordPuzzleSubmission) o;
            return Objects.equals(word, that.word);
        }

        @Override
        public int hashCode() {
            return Objects.hash(word);
        }

        @Override
        public String toString() {
            return "WordPuzzleSubmission{" +
                    "word='" + word + '\'' +
                    '}';
        }
    }

    // --------------------
    // equals / hashCode / toString
    // --------------------

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof QuizAnswerSubmission))
            return false;
        QuizAnswerSubmission that = (QuizAnswerSubmission) o;
        return Objects.equals(userAnswer, that.userAnswer)
                && Objects.equals(selectedChoiceId, that.selectedChoiceId)
                && Objects.equals(selectedChoiceIds, that.selectedChoiceIds)
                && Objects.equals(swipeRight, that.swipeRight)
                && Objects.equals(orderingIds, that.orderingIds)
                && Objects.equals(pairs, that.pairs)
                && Objects.equals(timeMs, that.timeMs)
                && Objects.equals(hangman, that.hangman)
                && Objects.equals(wordPuzzle, that.wordPuzzle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userAnswer, selectedChoiceId, selectedChoiceIds, swipeRight, orderingIds, pairs, timeMs,
                hangman, wordPuzzle);
    }

    @Override
    public String toString() {
        return "QuizAnswerSubmission{" +
                "userAnswer='" + userAnswer + '\'' +
                ", selectedChoiceId=" + selectedChoiceId +
                ", selectedChoiceIds=" + selectedChoiceIds +
                ", swipeRight=" + swipeRight +
                ", orderingIds=" + orderingIds +
                ", pairs=" + pairs +
                ", timeMs=" + timeMs +
                ", hangman=" + hangman +
                ", wordPuzzle=" + wordPuzzle +
                '}';
    }
}
