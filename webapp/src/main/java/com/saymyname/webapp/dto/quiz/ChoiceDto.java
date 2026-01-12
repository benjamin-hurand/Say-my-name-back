// src/main/java/com/saymyname/webapp/dto/quiz/ChoiceDto.java
package com.saymyname.webapp.dto.quiz;

/**
 * Enhanced choice DTO supporting:
 * - Text only (label + value)
 * - Photo only (photoUrl)
 * - Text + Photo combo
 * - Person references (personId)
 *
 * CRITICAL SECURITY NOTE:
 * This DTO NEVER includes a 'correct' flag or any truth/answer key data.
 * Backend validates and returns results, but never exposes answer key to client.
 * Truth is stored ONLY in QuizQuestionSnapshot.truth (backend-only).
 */
public record ChoiceDto(
        /**
         * Stable ID for frontend reconciliation (e.g., React key).
         * Not a database ID - just unique within this question.
         */
        Long id,

        /**
         * Display text (can be null for photo-only choices).
         */
        String label,

        /**
         * Canonical value (usually same as label, but normalized).
         * Client should send this value (or the ID) when answering.
         * Can be null for photo-only choices.
         */
        String value,

        /**
         * Photo URL (resolved by PhotoUrlResolver from storageKey).
         * Null if this choice has no photo.
         */
        String photoUrl,

        /**
         * Person ID if this choice represents a person.
         * Null for non-person choices.
         * Client can use this to fetch additional person details if needed.
         */
        Long personId

        // NOTE: 'correct' field deliberately omitted - NEVER expose to client
) {
}
