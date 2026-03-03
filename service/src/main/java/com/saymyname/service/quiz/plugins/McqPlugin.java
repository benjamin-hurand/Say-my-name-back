// src/main/java/com/saymyname/service/quiz/plugins/McqPlugin.java
package com.saymyname.service.quiz.plugins;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.TargetAnswerResult;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.enums.quiz.QuizPayloadType;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizChoice;
import com.saymyname.core.model.quiz.QuizLayoutHints;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.QuizQuestionDisplay;
import com.saymyname.core.model.quiz.QuizQuestionPayload;
import com.saymyname.core.model.quiz.QuizQuestionSpec;
import com.saymyname.core.model.quiz.QuizValidationResult;
import com.saymyname.core.model.quiz.answer.NormalizedChoice;
import com.saymyname.core.model.quiz.answer.NormalizedAudit;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.service.quiz.AnswerKeyService;
import com.saymyname.service.quiz.QuizSnapshotGuards;

/**
 * Multiple Choice Question plugin.
 *
 * Supports rich content:
 * - Text-only choices (current implementation)
 * - Photo-only choices (future enhancement via storageKey)
 * - Text + photo combos (future enhancement)
 *
 * ARCHITECTURE:
 * - Choices are DISPLAY-ONLY (no truth/answer key in QuizChoice)
 * - Truth extraction happens in QuizQuestionSnapshotFactory via:
 * 1. personId matching (for photo MCQs: "Who is this person?")
 * 2. value matching (for text MCQs: "What is X's last name?")
 * - Truth is stored in snapshot.truth (backend-only)
 * - Truth is NEVER sent to client (filtered in QuizQuestionDtoMapper)
 */
@Component
public class McqPlugin implements QuizQuestionPlugin, QuizQuestionPluginEnhanced {

    private static final int DEFAULT_CHOICES = 4;
    private static final int DEFAULT_POOL_SCAN = 30;

    private final AnswerKeyService answerKeyService;

    /**
     * Choice ids must be stable within one question payload, but they do not need
     * to be globally unique across questions. Using a per-plugin counter is OK.
     */
    private final AtomicLong seq = new AtomicLong(1L);

    public McqPlugin(AnswerKeyService answerKeyService) {
        this.answerKeyService = Objects.requireNonNull(answerKeyService, "answerKeyService");
    }

    @Override
    public QuizFormat supports() {
        return QuizFormat.MCQ;
    }

    @Override
    public QuizQuestion build(QuizQuestionSpec spec) {
        Objects.requireNonNull(spec, "spec");

        // Compute correct value for target person + attributes
        String value = answerKeyService
                .compute(spec.getPersonId(), spec.getTargetAttributeId());

        // Build distractors by scanning a randomized subset of the pool
        List<String> distractors = new ArrayList<>(DEFAULT_CHOICES - 1);

        List<Long> poolIds = new ArrayList<>(
                PluginSupport.poolIdsLimited(spec, DEFAULT_POOL_SCAN, spec.getPersonId()));

        // Randomize pool scan order to avoid always picking the same distractors
        if (poolIds.size() > 1) {
            Collections.shuffle(poolIds, ThreadLocalRandom.current());
        }

        for (Long pid : poolIds) {
            if (pid == null) {
                continue;
            }

            String v = answerKeyService
                    .compute(pid, spec.getTargetAttributeId());

            if (v == null) {
                continue;
            }
            String trimmed = v.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            // exclude the correct value
            if (!value.isEmpty() && trimmed.equalsIgnoreCase(value)) {
                continue;
            }

            // exclude duplicates (case-insensitive)
            boolean already = distractors.stream().anyMatch(d -> d.equalsIgnoreCase(trimmed));
            if (already) {
                continue;
            }

            distractors.add(trimmed);
            if (distractors.size() >= DEFAULT_CHOICES - 1) {
                break;
            }
        }

        // Build choices (correct + distractors + placeholders)
        List<QuizChoice> choices = new ArrayList<>(DEFAULT_CHOICES);

        // Correct choice: always included, but order will be shuffled later
        // CRITICAL: Truth extraction happens in QuizQuestionSnapshotFactory via:
        // 1. personId matching (for photo MCQs: "Who is this person?")
        // 2. value matching (for text MCQs: "What is X's last name?")
        // The 'correct' flag is NOT used (it's deprecated and always returns null)
        choices.add(new QuizChoice.Builder()
                .withId(nextChoiceId())
                .withLabel(value)
                .withValue(value)
                .withPersonId(spec.getPersonId()) // Used by snapshot factory for truth matching
                .build());

        for (String d : distractors) {
            choices.add(new QuizChoice.Builder()
                    .withId(nextChoiceId())
                    .withLabel(d)
                    .withValue(d)
                    // No personId for distractors (won't match target person)
                    .build());
        }

        // Fill remaining slots with empty placeholders (keeps payload size stable)
        while (choices.size() < DEFAULT_CHOICES) {
            choices.add(new QuizChoice.Builder()
                    .withId(nextChoiceId())
                    .withLabel("")
                    .withValue("")
                    .build());
        }

        // Randomize choice ordering so correct answer isn't always in the same position
        if (choices.size() > 1) {
            Collections.shuffle(choices, ThreadLocalRandom.current());
        }

        QuizQuestionDisplay display = new QuizQuestionDisplay.Builder()
                .withPrompt("Choisis la bonne reponse")
                .withSubtitle(null)
                .withInputPlaceholder(null)
                .build();

        // Layout hints for text-only MCQ (vertical list)
        QuizLayoutHints layoutHints = new QuizLayoutHints.Builder()
                .withLayout("list_vertical")
                .withItemStyle("text_only")
                .build();

        QuizQuestionPayload payload = new QuizQuestionPayload.Builder()
                .withType(QuizPayloadType.MCQ)
                .withChoices(choices)
                .withAllowMultiple(false)
                .withLayoutHints(layoutHints)
                .build();

        return PluginSupport.baseQuestion(
                spec,
                QuizFormat.MCQ,
                payload,
                null,
                display,
                null);
    }

    @Override
    public NormalizedAudit normalize(QuizQuestion question, QuizAnswerSubmission submission) {
        if (submission == null) {
            return new NormalizedChoice(null, null);
        }

        Long selectedId = submission.getSelectedChoiceId();
        String selectedValue = null;

        if (selectedId != null && question != null && question.getPayload() != null) {
            List<QuizChoice> choices = question.getPayload().getChoices();
            if (choices != null) {
                for (QuizChoice c : choices) {
                    if (c != null && Objects.equals(selectedId, c.getId())) {
                        selectedValue = c.getValue();
                        break;
                    }
                }
            }
        }

        if (selectedValue == null) {
            String ua = submission.getUserAnswer();
            selectedValue = ua == null ? null : ua.trim();
        }

        return new NormalizedChoice(selectedId, selectedValue);
    }

    @Override
    public QuizValidationResult validate(
            QuizQuestionSnapshot snapshot,
            QuizAnswerSubmission submission,
            NormalizedAudit normalized) {

        Objects.requireNonNull(snapshot, "snapshot");

        NormalizedChoice nc = (normalized instanceof NormalizedChoice c)
                ? c
                : new NormalizedChoice(null, null);

        List<String> correctKeys = snapshot.getTruth() == null
                ? List.of()
                : snapshot.getTruth().getCorrectChoiceKeys();

        boolean correct = false;

        // Primary: validate by selectedChoiceId against snapshot truth keys
        if (nc.selectedChoiceId() != null && correctKeys != null && !correctKeys.isEmpty()) {
            String key = String.valueOf(nc.selectedChoiceId());
            correct = correctKeys.contains(key);
        }

        // Fallback: validate by canonicalized value against correctAnswerDisplay
        if (!correct && nc.selectedValue() != null && snapshot.getTruth() != null) {
            String expected = snapshot.getTruth().getCorrectAnswerDisplay();
            correct = PluginSupport.equalsCanon(nc.selectedValue(), expected);
        }

        Long attrId = QuizSnapshotGuards.requireTargetAttributeId(snapshot, QuizFormat.MCQ);

        TargetAnswerResult result = PluginSupport.result(
                attrId,
                nc.selectedValue(),
                correct,
                true);

        return new QuizValidationResult.Builder()
                .withCorrect(correct)
                .withCorrectAnswerDisplay(
                        snapshot.getTruth() == null ? null : snapshot.getTruth().getCorrectAnswerDisplay())
                .withTargetAnswerResult(result)
                .build();
    }

    private Long nextChoiceId() {
        return seq.getAndIncrement();
    }

    // ========================================
    // QuizQuestionPluginEnhanced implementation
    // ========================================

    @Override
    public String generateFeedback(
            QuizQuestionSnapshot snapshot,
            QuizAnswerSubmission submission,
            NormalizedAudit normalized,
            QuizValidationResult validation) {

        if (validation.isCorrect()) {
            return "Correct ! 🎉";
        }

        // Find what user selected
        NormalizedChoice nc = (NormalizedChoice) normalized;
        String selected = nc.selectedValue();
        String correct = validation.getCorrectAnswerDisplay();

        if (selected != null && correct != null) {
            return String.format("Incorrect. Tu as choisi '%s' mais la bonne réponse est '%s'.",
                    selected, correct);
        }

        return "Incorrect. Essaie encore !";
    }

    @Override
    public Set<String> capabilities() {
        return Set.of(
                "supports_text_choices",
                "supports_single_selection"
        // Future: "supports_photos", "supports_rich_content",
        // "supports_multiple_selection"
        );
    }
}
