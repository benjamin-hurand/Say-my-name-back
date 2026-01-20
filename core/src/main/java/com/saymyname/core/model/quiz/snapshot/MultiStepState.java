// src/main/java/com/saymyname/core/model/quiz/snapshot/MultiStepState.java
package com.saymyname.core.model.quiz.snapshot;

/**
 * Marker interface for multi-step quiz format states.
 * Implemented by HangmanSnapshotState and WordPuzzleSnapshotState.
 * Provides type-safe alternative to Object for currentState/updatedState fields.
 */
public interface MultiStepState {
    /**
     * Validates the internal consistency of the state.
     * Called during construction and after mutations.
     */
    void validateInvariants();
}
