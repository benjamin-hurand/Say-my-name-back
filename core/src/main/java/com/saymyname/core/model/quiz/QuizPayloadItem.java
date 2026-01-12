// src/main/java/com/saymyname/core/model/quiz/QuizPayloadItem.java
package com.saymyname.core.model.quiz;

/**
 * Semantic wrapper for QuizDisplayItem used in multi-item formats (ASSOCIATION, ORDERING).
 *
 * Legacy fields:
 * - labelId: Kept for backward compatibility, maps to 'label' field
 *
 * Enhanced to support same features as QuizChoice:
 * - Text only (label/labelId)
 * - Photo only (storageKey + personId)
 * - Text + Photo combo
 * - Role hints for layout (e.g., "left", "right" for ASSOCIATION)
 *
 * CRITICAL: This class extends QuizDisplayItem which is DISPLAY-ONLY.
 * NO truth/answer key data is stored here.
 */
public class QuizPayloadItem extends QuizDisplayItem {

    public QuizPayloadItem() {
        super();
    }

    protected QuizPayloadItem(Builder b) {
        super(b);
    }

    /**
     * Legacy accessor for labelId (maps to label field).
     * Kept for backward compatibility with existing code.
     */
    public String getLabelId() {
        return getLabel();
    }

    /**
     * Legacy setter for labelId (maps to label field).
     * Kept for backward compatibility with existing code.
     */
    public void setLabelId(String labelId) {
        setLabel(labelId);
    }

    public static class Builder extends QuizDisplayItem.Builder<Builder> {

        /**
         * Legacy builder method for labelId (maps to label).
         * Kept for backward compatibility with existing code.
         */
        public Builder withLabelId(String v) {
            return (Builder) withLabel(v);
        }

        @Override
        public QuizPayloadItem build() {
            return new QuizPayloadItem(this);
        }
    }
}
