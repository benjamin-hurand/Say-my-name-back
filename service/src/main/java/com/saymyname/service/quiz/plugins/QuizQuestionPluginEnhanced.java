// src/main/java/com/saymyname/service/quiz/plugins/QuizQuestionPluginEnhanced.java
package com.saymyname.service.quiz.plugins;

import java.util.Set;

import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizValidationResult;
import com.saymyname.core.model.quiz.answer.NormalizedAudit;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;

/**
 * Optional enhancement interface for plugins that want to provide:
 * - Custom feedback generation
 * - Analytics/telemetry hooks
 * - Capability declarations
 *
 * Plugins implement this interface IN ADDITION to QuizQuestionPlugin.
 *
 * Design pattern: Capability interface (like Comparable, Serializable)
 * - Plugins opt-in by implementing this interface
 * - Framework checks instanceof before calling methods
 * - No breaking changes to existing plugins
 * - All methods have default implementations (no-op or null)
 */
public interface QuizQuestionPluginEnhanced {

    /**
     * Generate custom feedback message based on validation result.
     *
     * Use cases:
     * - MCQ: "You selected 'Paris' but the correct answer is 'London'"
     * - CLOZE: "You wrote 'Pariss' - check your spelling!"
     * - ORDERING: "You got 3 out of 5 in the correct position"
     *
     * @param snapshot   The frozen question snapshot
     * @param submission User's raw submission
     * @param normalized Normalized submission
     * @param validation Validation result
     * @return Custom feedback message (can be null to use default)
     */
    default String generateFeedback(
            QuizQuestionSnapshot snapshot,
            QuizAnswerSubmission submission,
            NormalizedAudit normalized,
            QuizValidationResult validation) {
        return null; // Default: no custom feedback
    }

    /**
     * Pre-validation hook for analytics/telemetry.
     *
     * Use cases:
     * - Track which distractors are most selected
     * - Measure time to first selection
     * - A/B testing different question formats
     *
     * @param snapshot   The frozen question snapshot
     * @param submission User's submission
     */
    default void onBeforeValidation(
            QuizQuestionSnapshot snapshot,
            QuizAnswerSubmission submission) {
        // Default: no-op
    }

    /**
     * Post-validation hook for analytics/telemetry.
     *
     * Use cases:
     * - Track error patterns
     * - Update distractor effectiveness metrics
     * - Log validation details for ML training
     *
     * @param snapshot   The frozen question snapshot
     * @param validation Validation result
     */
    default void onAfterValidation(
            QuizQuestionSnapshot snapshot,
            QuizValidationResult validation) {
        // Default: no-op
    }

    /**
     * Declares plugin capabilities/features.
     *
     * Use cases:
     * - "supports_photos" - plugin can handle photo-based questions
     * - "supports_timed" - plugin supports time-limited questions
     * - "supports_multiple_selection" - MCQ can have multiple correct answers
     * - "supports_rich_content" - supports text + photo combos
     *
     * @return Set of capability strings
     */
    default Set<String> capabilities() {
        return Set.of();
    }
}
