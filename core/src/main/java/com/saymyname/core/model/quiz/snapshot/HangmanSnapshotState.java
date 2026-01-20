// src/main/java/com/saymyname/core/model/quiz/snapshot/HangmanSnapshotState.java
package com.saymyname.core.model.quiz.snapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HangmanSnapshotState implements MultiStepState {

    /** Masque courant (affichage) : ex "T _ f f _ n y" ou "_______" */
    private String mask;

    /** Nombre d'erreurs déjà commises */
    private Integer errorsCount;

    /** Lettres tentées (majuscule canon) */
    private List<String> triedLetters = new ArrayList<>();

    /** Lettres fausses (majuscule canon) */
    private List<String> wrongLetters = new ArrayList<>();

    public HangmanSnapshotState() {
    }

    private HangmanSnapshotState(Builder b) {
        this.mask = b.mask;
        this.errorsCount = b.errorsCount;
        this.triedLetters = b.triedLetters != null ? b.triedLetters : new ArrayList<>();
        this.wrongLetters = b.wrongLetters != null ? b.wrongLetters : new ArrayList<>();
        validateInvariants();
    }

    @Override
    public void validateInvariants() {
        if (mask == null) {
            throw new IllegalStateException("HangmanSnapshotState.mask is required");
        }
        if (errorsCount == null || errorsCount < 0) {
            throw new IllegalStateException("HangmanSnapshotState.errorsCount must be >= 0");
        }
        if (triedLetters == null) {
            throw new IllegalStateException("HangmanSnapshotState.triedLetters cannot be null");
        }
        if (wrongLetters == null) {
            throw new IllegalStateException("HangmanSnapshotState.wrongLetters cannot be null");
        }
        if (triedLetters.stream().anyMatch(Objects::isNull)) {
            throw new IllegalStateException("HangmanSnapshotState.triedLetters cannot contain null");
        }
        if (wrongLetters.stream().anyMatch(Objects::isNull)) {
            throw new IllegalStateException("HangmanSnapshotState.wrongLetters cannot contain null");
        }
        // wrongLetters ⊆ triedLetters (fortement recommandé)
        if (!triedLetters.containsAll(wrongLetters)) {
            throw new IllegalStateException("HangmanSnapshotState.wrongLetters must be subset of triedLetters");
        }
    }

    // ---------------- Getters/Setters ----------------

    public String getMask() {
        return mask;
    }

    public Integer getErrorsCount() {
        return errorsCount;
    }

    public List<String> getTriedLetters() {
        return triedLetters;
    }

    public List<String> getWrongLetters() {
        return wrongLetters;
    }

    public void setMask(String mask) {
        this.mask = mask;
    }

    public void setErrorsCount(Integer errorsCount) {
        this.errorsCount = errorsCount;
    }

    public void setTriedLetters(List<String> triedLetters) {
        this.triedLetters = triedLetters;
    }

    public void setWrongLetters(List<String> wrongLetters) {
        this.wrongLetters = wrongLetters;
    }

    // ---------------- Builder ----------------

    public static class Builder {
        private String mask;
        private Integer errorsCount;
        private List<String> triedLetters;
        private List<String> wrongLetters;

        public Builder withMask(String v) {
            this.mask = v;
            return this;
        }

        public Builder withErrorsCount(Integer v) {
            this.errorsCount = v;
            return this;
        }

        public Builder withTriedLetters(List<String> v) {
            this.triedLetters = v;
            return this;
        }

        public Builder withWrongLetters(List<String> v) {
            this.wrongLetters = v;
            return this;
        }

        public HangmanSnapshotState build() {
            return new HangmanSnapshotState(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof HangmanSnapshotState))
            return false;
        HangmanSnapshotState that = (HangmanSnapshotState) o;
        return Objects.equals(mask, that.mask)
                && Objects.equals(errorsCount, that.errorsCount)
                && Objects.equals(triedLetters, that.triedLetters)
                && Objects.equals(wrongLetters, that.wrongLetters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mask, errorsCount, triedLetters, wrongLetters);
    }
}
