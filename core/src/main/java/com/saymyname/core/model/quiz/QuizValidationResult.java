// src/main/java/com/saymyname/core/model/quiz/QuizValidationResult.java
package com.saymyname.core.model.quiz;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.saymyname.core.model.course.ResultAttribute;
import com.saymyname.core.model.quiz.snapshot.MultiStepState;

/**
 * Résultat de validation d'une réponse utilisateur contre un snapshot figé.
 * Immuable, audit-proof, UI-friendly.
 */
public final class QuizValidationResult {

        private final boolean correct;

        /**
         * Réponse correcte affichable côté UI (ex: "Jean Dupont", "Oui", "A → B").
         * Peut être null si non pertinent.
         */
        private final String correctAnswerDisplay;

        /**
         * Détails par attribut / item (optionnel).
         * Type unifié avec Course/DTO existants.
         */
        private final List<ResultAttribute> resultAttributes;

        /**
         * Message d'erreur de validation (pour formats multi-step).
         * Ex: "Letter already tried", "Word must be 5 letters"
         */
        private final String errorMessage;

        /**
         * État mis à jour pour formats multi-step (HangmanSnapshotState ou
         * WordPuzzleSnapshotState).
         * Null pour formats single-shot.
         */
        private final MultiStepState updatedState;

        /**
         * Indicateur de complétion pour formats multi-step.
         * - null: format single-shot (MCQ, TEXT_INPUT, etc.)
         * - false: multi-step incomplet (continuer sur même question)
         * - true: multi-step terminé (finaliser et générer next)
         */
        private final Boolean isComplete;

        private QuizValidationResult(Builder b) {
                this.correct = b.correct;
                this.correctAnswerDisplay = b.correctAnswerDisplay;
                this.resultAttributes = b.resultAttributes != null ? List.copyOf(b.resultAttributes) : List.of();
                this.errorMessage = b.errorMessage;
                this.updatedState = b.updatedState;
                this.isComplete = b.isComplete;
                validateInvariants();
        }

        private void validateInvariants() {
                for (ResultAttribute ra : resultAttributes) {
                        if (ra == null) {
                                throw new IllegalStateException(
                                                "QuizValidationResult.resultAttributes cannot contain null");
                        }
                }
        }

        public boolean isCorrect() {
                return correct;
        }

        public String getCorrectAnswerDisplay() {
                return correctAnswerDisplay;
        }

        public List<ResultAttribute> getResultAttributes() {
                return resultAttributes;
        }

        public String getErrorMessage() {
                return errorMessage;
        }

        public MultiStepState getUpdatedState() {
                return updatedState;
        }

        public Boolean getIsComplete() {
                return isComplete;
        }

        public static class Builder {

                private boolean correct;
                private String correctAnswerDisplay;
                private List<ResultAttribute> resultAttributes;
                private String errorMessage;
                private MultiStepState updatedState;
                private Boolean isComplete;

                public Builder withCorrect(boolean v) {
                        this.correct = v;
                        return this;
                }

                public Builder withCorrectAnswerDisplay(String v) {
                        this.correctAnswerDisplay = v;
                        return this;
                }

                public Builder withResultAttributes(List<ResultAttribute> v) {
                        if (v != null && v.stream().anyMatch(Objects::isNull)) {
                                throw new IllegalArgumentException("resultAttributes cannot contain null");
                        }
                        this.resultAttributes = v;
                        return this;
                }

                public Builder addResultAttribute(ResultAttribute v) {
                        if (v == null) {
                                throw new IllegalArgumentException("ResultAttribute cannot be null");
                        }
                        if (this.resultAttributes == null) {
                                this.resultAttributes = new ArrayList<>();
                        }
                        this.resultAttributes.add(v);
                        return this;
                }

                public Builder withErrorMessage(String v) {
                        this.errorMessage = v;
                        return this;
                }

                public Builder withUpdatedState(MultiStepState v) {
                        this.updatedState = v;
                        return this;
                }

                public Builder withIsComplete(Boolean v) {
                        this.isComplete = v;
                        return this;
                }

                public QuizValidationResult build() {
                        return new QuizValidationResult(this);
                }
        }

        @Override
        public boolean equals(Object o) {
                if (this == o)
                        return true;
                if (!(o instanceof QuizValidationResult that))
                        return false;
                return correct == that.correct
                                && Objects.equals(correctAnswerDisplay, that.correctAnswerDisplay)
                                && Objects.equals(resultAttributes, that.resultAttributes)
                                && Objects.equals(errorMessage, that.errorMessage)
                                && Objects.equals(updatedState, that.updatedState)
                                && Objects.equals(isComplete, that.isComplete);
        }

        @Override
        public int hashCode() {
                return Objects.hash(correct, correctAnswerDisplay, resultAttributes,
                                errorMessage, updatedState, isComplete);
        }

        @Override
        public String toString() {
                return "QuizValidationResult{" +
                                "correct=" + correct +
                                ", correctAnswerDisplay='" + correctAnswerDisplay + '\'' +
                                ", resultAttributes=" + resultAttributes +
                                ", errorMessage='" + errorMessage + '\'' +
                                ", updatedState=" + updatedState +
                                ", isComplete=" + isComplete +
                                '}';
        }
}
