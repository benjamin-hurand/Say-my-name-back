// src/main/java/com/saymyname/core/model/quiz/QuizValidationResult.java
package com.saymyname.core.model.quiz;

import java.util.Objects;

import com.saymyname.core.model.course.TargetAnswerResult;
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
        private final TargetAnswerResult targetAnswerResult;

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
                this.targetAnswerResult = b.targetAnswerResult;
                this.errorMessage = b.errorMessage;
                this.updatedState = b.updatedState;
                this.isComplete = b.isComplete;
        }

        public boolean isCorrect() {
                return correct;
        }

        public String getCorrectAnswerDisplay() {
                return correctAnswerDisplay;
        }

        public TargetAnswerResult getTargetAnswerResult() {
                return targetAnswerResult;
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
                private TargetAnswerResult targetAnswerResult;
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

                public Builder withTargetAnswerResult(TargetAnswerResult v) {
                        this.targetAnswerResult = v;
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
                                && Objects.equals(targetAnswerResult, that.targetAnswerResult)
                                && Objects.equals(errorMessage, that.errorMessage)
                                && Objects.equals(updatedState, that.updatedState)
                                && Objects.equals(isComplete, that.isComplete);
        }

        @Override
        public int hashCode() {
                return Objects.hash(correct, correctAnswerDisplay, targetAnswerResult,
                                errorMessage, updatedState, isComplete);
        }

        @Override
        public String toString() {
                return "QuizValidationResult{" +
                                "correct=" + correct +
                                ", correctAnswerDisplay='" + correctAnswerDisplay + '\'' +
                                ", targetAnswerResult=" + targetAnswerResult +
                                ", errorMessage='" + errorMessage + '\'' +
                                ", updatedState=" + updatedState +
                                ", isComplete=" + isComplete +
                                '}';
        }
}
