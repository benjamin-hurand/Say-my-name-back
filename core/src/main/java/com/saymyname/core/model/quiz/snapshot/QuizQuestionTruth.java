// src/main/java/com/saymyname/core/model/quiz/snapshot/QuizQuestionTruth.java
package com.saymyname.core.model.quiz.snapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.saymyname.core.model.enums.quiz.snapshot.QuizTruthType;

public class QuizQuestionTruth {

    /**
     * Type de vérité (piloté par format).
     * - TEXT: TEXT_INPUT / CLOZE / HANGMAN
     * - MCQ: MCQ / TAP_CHOICE
     * - BINARY_SWIPE: swipe
     * - ORDERING: ordering
     * - ASSOCIATION: matching pairs
     */
    private QuizTruthType type;

    // ----------------- Common meta -----------------

    /**
     * Réponses cibles déjà normalisées (format TEXT).
     * Ex: prénom => ["jean"]
     * Ex: prénom + nom => ["benjamin hurand"] (ou plusieurs variantes selon ton
     * design)
     */
    private List<String> expectedNormalizedAnswers = new ArrayList<>();

    /**
     * Variantes acceptées déjà normalisées (format TEXT).
     * Peut inclure les expected également; ton validateur peut faire union.
     */
    private List<String> acceptedNormalizedAnswers = new ArrayList<>();

    /**
     * Vérité "figée" basée sur les EAV (attributeId -> value) utilisée au moment
     * où la question a été posée/validée.
     *
     * But: audit-proof / replay-proof.
     *
     * Recommandé pour TEXT (et aussi utile pour d'autres formats si tu veux du
     * feedback structuré).
     */
    private List<TruthAttributeValue> targetAttributeValues = new ArrayList<>();

    /**
     * Clés de choix correctes (format MCQ / TAP_CHOICE).
     * Ce sont des keys du snapshot (pas DB id).
     */
    private List<String> correctChoiceKeys = new ArrayList<>();

    /**
     * Swipe: true si la bonne réponse est swipeRight.
     */
    private Boolean correctSwipeRight;

    /**
     * Ordering: liste des keys attendues dans l'ordre.
     */
    private List<String> correctItemKeysInOrder = new ArrayList<>();

    /**
     * Association: paires attendues leftKey -> rightKey.
     */
    private List<TruthPair> correctPairs = new ArrayList<>();

    /**
     * Règles de matching / audit (optionnel mais utile).
     */
    private QuizTruthRules rules;

    /**
     * Réponse "humaine" optionnelle pour feedback / audit (pas utilisée pour
     * valider).
     * Exemple: "Benjamin Hurand"
     */
    private String correctAnswerDisplay;

    public QuizQuestionTruth() {
    }

    private QuizQuestionTruth(Builder b) {
        this.type = b.type;

        this.expectedNormalizedAnswers = b.expectedNormalizedAnswers != null ? b.expectedNormalizedAnswers
                : new ArrayList<>();
        this.acceptedNormalizedAnswers = b.acceptedNormalizedAnswers != null ? b.acceptedNormalizedAnswers
                : new ArrayList<>();

        this.targetAttributeValues = b.targetAttributeValues != null ? b.targetAttributeValues : new ArrayList<>();

        this.correctChoiceKeys = b.correctChoiceKeys != null ? b.correctChoiceKeys : new ArrayList<>();
        this.correctSwipeRight = b.correctSwipeRight;

        this.correctItemKeysInOrder = b.correctItemKeysInOrder != null ? b.correctItemKeysInOrder : new ArrayList<>();
        this.correctPairs = b.correctPairs != null ? b.correctPairs : new ArrayList<>();

        this.rules = b.rules;
        this.correctAnswerDisplay = b.correctAnswerDisplay;

        validateInvariants();
    }

    public void validateInvariants() {
        if (type == null) {
            throw new IllegalStateException("QuizQuestionTruth.type is required");
        }

        if (rules != null) {
            rules.validateInvariants();
        }

        switch (type) {
            case TEXT -> validateTextTruth();
            case MCQ -> validateMcqTruth();
            case BINARY_SWIPE -> validateSwipeTruth();
            case ORDERING -> validateOrderingTruth();
            case ASSOCIATION -> validateAssociationTruth();
            default -> throw new IllegalStateException("Unsupported truth type: " + type);
        }
    }

    private void validateTextTruth() {
        boolean hasExpected = expectedNormalizedAnswers != null && !expectedNormalizedAnswers.isEmpty();
        boolean hasAccepted = acceptedNormalizedAnswers != null && !acceptedNormalizedAnswers.isEmpty();
        boolean hasFrozenEav = targetAttributeValues != null && !targetAttributeValues.isEmpty();

        if (!hasExpected && !hasAccepted && !hasFrozenEav) {
            throw new IllegalStateException(
                    "TEXT truth must contain expectedNormalizedAnswers, acceptedNormalizedAnswers, or targetAttributeValues");
        }

        if (expectedNormalizedAnswers != null) {
            for (String s : expectedNormalizedAnswers) {
                if (s == null || s.isBlank()) {
                    throw new IllegalStateException("TEXT truth expectedNormalizedAnswers cannot contain null/blank");
                }
            }
        }
        if (acceptedNormalizedAnswers != null) {
            for (String s : acceptedNormalizedAnswers) {
                if (s == null || s.isBlank()) {
                    throw new IllegalStateException("TEXT truth acceptedNormalizedAnswers cannot contain null/blank");
                }
            }
        }

        if (targetAttributeValues != null) {
            for (TruthAttributeValue tav : targetAttributeValues) {
                if (tav == null) {
                    throw new IllegalStateException("TEXT truth targetAttributeValues cannot contain null");
                }
                tav.validateInvariants();
            }
        }
    }

    private void validateMcqTruth() {
        if (correctChoiceKeys == null || correctChoiceKeys.isEmpty()) {
            throw new IllegalStateException("MCQ truth must contain at least 1 correctChoiceKey");
        }
        for (String k : correctChoiceKeys) {
            if (k == null || k.isBlank()) {
                throw new IllegalStateException("MCQ truth correctChoiceKeys cannot contain null/blank");
            }
        }
        // MCQ: expectedNormalizedAnswers / targetAttributeValues facultatifs
    }

    private void validateSwipeTruth() {
        if (correctSwipeRight == null) {
            throw new IllegalStateException("BINARY_SWIPE truth must set correctSwipeRight");
        }
    }

    private void validateOrderingTruth() {
        if (correctItemKeysInOrder == null || correctItemKeysInOrder.isEmpty()) {
            throw new IllegalStateException("ORDERING truth must contain correctItemKeysInOrder");
        }
        for (String k : correctItemKeysInOrder) {
            if (k == null || k.isBlank()) {
                throw new IllegalStateException("ORDERING truth correctItemKeysInOrder cannot contain null/blank");
            }
        }
        // éviter doublons
        Set<String> set = new HashSet<>(correctItemKeysInOrder);
        if (set.size() != correctItemKeysInOrder.size()) {
            throw new IllegalStateException("ORDERING truth correctItemKeysInOrder must not contain duplicates");
        }
    }

    private void validateAssociationTruth() {
        if (correctPairs == null || correctPairs.isEmpty()) {
            throw new IllegalStateException("ASSOCIATION truth must contain correctPairs");
        }
        Set<String> left = new HashSet<>();
        Set<String> right = new HashSet<>();
        for (TruthPair p : correctPairs) {
            if (p == null) {
                throw new IllegalStateException("ASSOCIATION truth correctPairs cannot contain null");
            }
            p.validateInvariants();

            if (!left.add(p.getLeftKey())) {
                throw new IllegalStateException("ASSOCIATION truth cannot repeat leftKey=" + p.getLeftKey());
            }
            if (!right.add(p.getRightKey())) {
                throw new IllegalStateException("ASSOCIATION truth cannot repeat rightKey=" + p.getRightKey());
            }
        }
    }

    // ----------------- Getters/Setters -----------------

    public QuizTruthType getType() {
        return type;
    }

    public void setType(QuizTruthType type) {
        this.type = type;
    }

    public List<String> getExpectedNormalizedAnswers() {
        return expectedNormalizedAnswers;
    }

    public void setExpectedNormalizedAnswers(List<String> expectedNormalizedAnswers) {
        this.expectedNormalizedAnswers = expectedNormalizedAnswers;
    }

    public List<String> getAcceptedNormalizedAnswers() {
        return acceptedNormalizedAnswers;
    }

    public void setAcceptedNormalizedAnswers(List<String> acceptedNormalizedAnswers) {
        this.acceptedNormalizedAnswers = acceptedNormalizedAnswers;
    }

    public List<TruthAttributeValue> getTargetAttributeValues() {
        return targetAttributeValues;
    }

    public void setTargetAttributeValues(List<TruthAttributeValue> targetAttributeValues) {
        this.targetAttributeValues = targetAttributeValues;
    }

    public List<String> getCorrectChoiceKeys() {
        return correctChoiceKeys;
    }

    public void setCorrectChoiceKeys(List<String> correctChoiceKeys) {
        this.correctChoiceKeys = correctChoiceKeys;
    }

    public Boolean getCorrectSwipeRight() {
        return correctSwipeRight;
    }

    public void setCorrectSwipeRight(Boolean correctSwipeRight) {
        this.correctSwipeRight = correctSwipeRight;
    }

    public List<String> getCorrectItemKeysInOrder() {
        return correctItemKeysInOrder;
    }

    public void setCorrectItemKeysInOrder(List<String> correctItemKeysInOrder) {
        this.correctItemKeysInOrder = correctItemKeysInOrder;
    }

    public List<TruthPair> getCorrectPairs() {
        return correctPairs;
    }

    public void setCorrectPairs(List<TruthPair> correctPairs) {
        this.correctPairs = correctPairs;
    }

    public QuizTruthRules getRules() {
        return rules;
    }

    public void setRules(QuizTruthRules rules) {
        this.rules = rules;
    }

    public String getCorrectAnswerDisplay() {
        return correctAnswerDisplay;
    }

    public void setCorrectAnswerDisplay(String correctAnswerDisplay) {
        this.correctAnswerDisplay = correctAnswerDisplay;
    }

    // ----------------- Builder -----------------

    public static class Builder {
        private QuizTruthType type;

        private List<String> expectedNormalizedAnswers;
        private List<String> acceptedNormalizedAnswers;

        private List<TruthAttributeValue> targetAttributeValues;

        private List<String> correctChoiceKeys;
        private Boolean correctSwipeRight;

        private List<String> correctItemKeysInOrder;
        private List<TruthPair> correctPairs;

        private QuizTruthRules rules;
        private String correctAnswerDisplay;

        public Builder withType(QuizTruthType v) {
            this.type = v;
            return this;
        }

        public Builder withExpectedNormalizedAnswers(List<String> v) {
            this.expectedNormalizedAnswers = v;
            return this;
        }

        public Builder withAcceptedNormalizedAnswers(List<String> v) {
            this.acceptedNormalizedAnswers = v;
            return this;
        }

        public Builder withTargetAttributeValues(List<TruthAttributeValue> v) {
            this.targetAttributeValues = v;
            return this;
        }

        public Builder withCorrectChoiceKeys(List<String> v) {
            this.correctChoiceKeys = v;
            return this;
        }

        public Builder withCorrectSwipeRight(Boolean v) {
            this.correctSwipeRight = v;
            return this;
        }

        public Builder withCorrectItemKeysInOrder(List<String> v) {
            this.correctItemKeysInOrder = v;
            return this;
        }

        public Builder withCorrectPairs(List<TruthPair> v) {
            this.correctPairs = v;
            return this;
        }

        public Builder withRules(QuizTruthRules v) {
            this.rules = v;
            return this;
        }

        public Builder withCorrectAnswerDisplay(String v) {
            this.correctAnswerDisplay = v;
            return this;
        }

        public QuizQuestionTruth build() {
            return new QuizQuestionTruth(this);
        }
    }

    // ----------------- equals/hashCode -----------------

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof QuizQuestionTruth))
            return false;
        QuizQuestionTruth that = (QuizQuestionTruth) o;
        return type == that.type
                && Objects.equals(expectedNormalizedAnswers, that.expectedNormalizedAnswers)
                && Objects.equals(acceptedNormalizedAnswers, that.acceptedNormalizedAnswers)
                && Objects.equals(targetAttributeValues, that.targetAttributeValues)
                && Objects.equals(correctChoiceKeys, that.correctChoiceKeys)
                && Objects.equals(correctSwipeRight, that.correctSwipeRight)
                && Objects.equals(correctItemKeysInOrder, that.correctItemKeysInOrder)
                && Objects.equals(correctPairs, that.correctPairs)
                && Objects.equals(rules, that.rules)
                && Objects.equals(correctAnswerDisplay, that.correctAnswerDisplay);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, expectedNormalizedAnswers, acceptedNormalizedAnswers,
                targetAttributeValues, correctChoiceKeys, correctSwipeRight, correctItemKeysInOrder,
                correctPairs, rules, correctAnswerDisplay);
    }
}
