// src/main/java/com/saymyname/core/model/quiz/QuizChoice.java
package com.saymyname.core.model.quiz;

/**
 * Semantic wrapper for QuizDisplayItem used in choice-based formats (MCQ, BINARY_SWIPE).
 * Provides domain-specific naming while using the unified display model underneath.
 *
 * Enhanced to support:
 * - Text only (label + value)
 * - Photo only (storageKey + personId)
 * - Text + Photo combo
 * - Person references (personId for avatar lookup)
 *
 * CRITICAL: This class extends QuizDisplayItem which is DISPLAY-ONLY.
 * NO truth/answer key data is stored here.
 * For backward compatibility, getCorrect()/setCorrect() are kept but DEPRECATED.
 * Use QuizQuestionSnapshot.truth for validation instead.
 */
public class QuizChoice extends QuizDisplayItem {

    public QuizChoice() {
        super();
    }

    protected QuizChoice(Builder b) {
        super(b);
    }

    /**
     * @deprecated Use QuizQuestionSnapshot.truth for answer validation.
     * This method exists only for backward compatibility and always returns null.
     * Truth is stored ONLY in snapshot, never in display models.
     */
    @Deprecated
    public Boolean getCorrect() {
        return null;
    }

    /**
     * @deprecated Truth should be stored in QuizQuestionSnapshot.truth, not in display models.
     * This method exists only for backward compatibility and does nothing.
     */
    @Deprecated
    public void setCorrect(Boolean correct) {
        // No-op: Truth is stored in snapshot, not in display models
    }

    public static class Builder extends QuizDisplayItem.Builder<Builder> {

        /**
         * @deprecated Use snapshot.truth for answer validation instead.
         * This method exists only for backward compatibility and does nothing.
         */
        @Deprecated
        public Builder withCorrect(Boolean v) {
            // No-op: Truth is stored in snapshot, not in display models
            return this;
        }

        @Override
        public QuizChoice build() {
            return new QuizChoice(this);
        }
    }
}
