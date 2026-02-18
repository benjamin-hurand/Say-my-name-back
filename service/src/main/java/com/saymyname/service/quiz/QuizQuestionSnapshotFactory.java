// src/main/java/com/saymyname/service/quiz/QuizQuestionSnapshotFactory.java
package com.saymyname.service.quiz;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.enums.quiz.snapshot.QuizTruthType;
import com.saymyname.core.model.people.Fact;
import com.saymyname.core.model.quiz.QuizChoice;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.QuizQuestionPayload;
import com.saymyname.core.model.quiz.snapshot.HangmanRules;
import com.saymyname.core.model.quiz.snapshot.HangmanSnapshotState;
import com.saymyname.core.model.quiz.snapshot.MultiStepState;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.core.model.quiz.snapshot.WordPuzzleRules;
import com.saymyname.core.model.quiz.snapshot.WordPuzzleSnapshotState;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionTruth;
import com.saymyname.core.model.quiz.snapshot.QuizTruthRules;
import com.saymyname.core.model.quiz.snapshot.TruthAttributeValue;
import com.saymyname.core.model.quiz.snapshot.TruthPair;
import com.saymyname.persistence.dao.FactDao;

/**
 * Construit un snapshot auto-suffisant (truth + payload) sans dépendance au
 * token.
 */
@Component
public class QuizQuestionSnapshotFactory {

    // ✅ bump schema version (ajout hangman state/rules)
    private static final int SNAPSHOT_SCHEMA_VERSION = 5;
    private static final String GENERATOR_VERSION = "quiz-pipeline-v1";
    private static final String NORMALIZER_VERSION = "normalizer-v1";

    private final FactDao personAttributeDao;

    public QuizQuestionSnapshotFactory(FactDao personAttributeDao) {
        this.personAttributeDao = Objects.requireNonNull(personAttributeDao, "personAttributeDao");
    }

    /**
     * Capture les valeurs EAV cibles au moment de l'émission.
     */
    public List<TruthAttributeValue> freezeTruthForQuestion(QuizQuestion q) {
        Objects.requireNonNull(q, "quizQuestion");
        if (q.getPersonId() == null) {
            throw new IllegalStateException("Cannot freeze truth: question.personId is null");
        }
        if (q.getTargetAttributeIds() == null || q.getTargetAttributeIds().isEmpty()) {
            throw new IllegalStateException("Cannot freeze truth: question.targetAttributeIds is empty");
        }

        List<Fact> personAttrs = personAttributeDao.findFactsByPersonId(q.getPersonId());

        List<TruthAttributeValue> out = new ArrayList<>();
        for (Long attrId : q.getTargetAttributeIds()) {
            if (attrId == null)
                continue;

            List<Fact> matches = personAttrs.stream()
                    .filter(pa -> pa != null && pa.getAttribute() != null && attrId.equals(pa.getAttribute().getId()))
                    .toList();

            for (Fact pa : matches) {
                if (pa.getValue() == null || pa.getValue().isBlank())
                    continue;

                out.add(TruthAttributeValue.builder()
                        .attributeId(attrId)
                        .value(pa.getValue())
                        .build());
            }
        }

        if (out.isEmpty()) {
            throw new IllegalStateException(
                    "Frozen truth is empty for personId=" + q.getPersonId()
                            + " targetAttributeIds=" + q.getTargetAttributeIds());
        }

        return out;
    }

    public QuizQuestionSnapshot fromQuestion(QuizQuestion q, List<TruthAttributeValue> frozenTargetValues) {
        return fromQuestion(q, frozenTargetValues, null);
    }

    public QuizQuestionSnapshot fromQuestion(
            QuizQuestion q,
            List<TruthAttributeValue> frozenTargetValues,
            List<Long> targetPersonIdsOverride) {

        Objects.requireNonNull(q, "quizQuestion");

        if (q.getFormat() == null)
            throw new IllegalStateException("QuizQuestion.format is required");
        if (q.getContext() == null)
            throw new IllegalStateException("QuizQuestion.context is required");
        if (q.getGameModeId() == null)
            throw new IllegalStateException("QuizQuestion.gameModeId is required");
        if (q.getTargetAttributeIds() == null || q.getTargetAttributeIds().isEmpty())
            throw new IllegalStateException("QuizQuestion.targetAttributeIds is required");
        if (q.getOperator() == null || q.getOperator().isBlank())
            throw new IllegalStateException("QuizQuestion.operator is required");
        if (q.getPersonId() == null)
            throw new IllegalStateException("QuizQuestion.personId is required (single-target model)");
        if (q.getReasonCode() == null)
            throw new IllegalStateException("QuizQuestion.reasonCode is required");

        QuizQuestionTruth truth = buildTruth(q, frozenTargetValues);

        Boolean timed = null;
        Integer timeLimitMs = null;
        if (q.getDisplay() != null) {
            timed = q.getDisplay().getTimed();
            timeLimitMs = q.getDisplay().getTimeLimitMs();
        }

        List<Long> resolvedTargets = new ArrayList<>();
        if (targetPersonIdsOverride != null) {
            for (Long id : targetPersonIdsOverride) {
                if (id != null) {
                    resolvedTargets.add(id);
                }
            }
        }
        if (q.getPersonId() != null && !resolvedTargets.contains(q.getPersonId())) {
            resolvedTargets.add(q.getPersonId());
        }
        if (resolvedTargets.isEmpty()) {
            resolvedTargets.add(q.getPersonId());
        }

        // ------------------------------------------------------------
        // ✅ HANGMAN snapshot state/rules (stateful, replayable)
        // ------------------------------------------------------------
        HangmanRules hangmanRules = null;
        HangmanSnapshotState hangmanState = null;

        if (q.getFormat() == QuizFormat.HANGMAN) {
            QuizQuestionPayload payload = q.getPayload();

            // mask initial (doit être présent côté payload en build() du plugin)
            String mask = (payload != null ? payload.getMask() : null);
            if (mask == null) {
                throw new IllegalStateException("HANGMAN requires payload.mask to initialize snapshot");
            }

            Integer maxErrors = (payload != null ? payload.getMaxErrors() : null);
            if (maxErrors == null) {
                // fallback très prudent
                maxErrors = 6;
            }

            // Règles (tu peux ajuster selon ton produit)
            hangmanRules = HangmanRules.builder()
                    .maxErrors(maxErrors)
                    .normalized(true) // ignore accents/case (recommandé)
                    .canSolveWholeWord(true) // option UX
                    .alphabet(List.of(
                            "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
                            "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"))
                    .build();

            // État initial
            hangmanState = HangmanSnapshotState.builder()
                    .mask(mask)
                    .errorsCount(0)
                    .triedLetters(new ArrayList<>())
                    .wrongLetters(new ArrayList<>())
                    .build();
        }

        // ------------------------------------------------------------
        // ✅ WORD_PUZZLE snapshot state/rules (stateful, replayable)
        // ------------------------------------------------------------
        WordPuzzleRules wordPuzzleRules = null;
        WordPuzzleSnapshotState wordPuzzleState = null;

        if (q.getFormat() == QuizFormat.WORD_PUZZLE) {
            QuizQuestionPayload payload = q.getPayload();

            Integer maxAttempts = (payload != null ? payload.getMaxAttempts() : null);
            if (maxAttempts == null) {
                maxAttempts = 6; // Default Wordle-style
            }

            Integer wordLength = (payload != null ? payload.getWordLength() : null);
            if (wordLength == null) {
                // Derive from frozen target values
                String solution = frozenTargetValues.get(0).getValue();
                wordLength = solution.length();
            }

            // Rules
            wordPuzzleRules = WordPuzzleRules.builder()
                    .maxAttempts(maxAttempts)
                    .wordLength(wordLength)
                    .caseSensitive(false)
                    .allowRepeatedLetters(true)
                    .build();

            // Initial state
            wordPuzzleState = WordPuzzleSnapshotState.builder()
                    .attempts(new ArrayList<>())
                    .attemptsRemaining(maxAttempts)
                    .solved(false)
                    .failed(false)
                    .build();
        }

        return QuizQuestionSnapshot.builder()
                .snapshotSchemaVersion(SNAPSHOT_SCHEMA_VERSION)
                .generatorVersion(GENERATOR_VERSION)
                .normalizerVersion(NORMALIZER_VERSION)

                .format(q.getFormat())
                .context(q.getContext())

                // auditable spec
                .gameModeId(q.getGameModeId())
                .targetAttributeIds(new ArrayList<>(q.getTargetAttributeIds()))
                .operator(q.getOperator())
                .personId(q.getPersonId())
                .storageKey(q.getStorageKey())

                // renderable parts
                .display(q.getDisplay())
                .hints(q.getHints())
                .payload(q.getPayload())
                .followUp(q.getFollowUp())
                .timed(timed)
                .timeLimitMs(timeLimitMs)
                .reasonCode(q.getReasonCode())
                .reasonDetailsJson(q.getReasonDetailsJson())

                // truth
                .truth(truth)
                .targetPersonIds(resolvedTargets)

                // ✅ hangman extras
                .hangmanRules(hangmanRules)
                .hangmanState(hangmanState)

                // ✅ word puzzle extras
                .wordPuzzleRules(wordPuzzleRules)
                .wordPuzzleState(wordPuzzleState)

                .build();
    }

    private QuizQuestionTruth buildTruth(QuizQuestion q, List<TruthAttributeValue> frozenTargetValues) {

        QuizTruthRules rules = QuizTruthRules.builder()
                .operator(q.getOperator())
                .build();

        QuizFormat effectiveFormat = q.getFormat();
        QuizQuestionPayload effectivePayload = q.getPayload();

        QuizTruthType type = toTruthType(effectiveFormat);

        QuizQuestionTruth.Builder tb = QuizQuestionTruth.builder()
                .type(type)
                .rules(rules);

        if (type == QuizTruthType.TEXT) {
            if (frozenTargetValues == null || frozenTargetValues.isEmpty()) {
                throw new IllegalStateException("TEXT truth requires frozenTargetValues");
            }
            tb.targetAttributeValues(new ArrayList<>(frozenTargetValues));

            String display = frozenTargetValues.stream()
                    .map(TruthAttributeValue::getValue)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .reduce((a, b) -> a + " " + b)
                    .orElse(null);

            tb.correctAnswerDisplay(display);
            return tb.build();
        }

        if (type == QuizTruthType.MCQ) {
            List<String> correctKeys = extractCorrectChoiceKeys(
                    effectivePayload,
                    frozenTargetValues,
                    q.getPersonId());
            tb.correctChoiceKeys(correctKeys);
            tb.correctAnswerDisplay(firstChoiceLabel(effectivePayload, correctKeys));
            return tb.build();
        }

        if (type == QuizTruthType.BINARY_SWIPE) {
            tb.correctSwipeRight(Boolean.TRUE);
            tb.correctAnswerDisplay("true");
            return tb.build();
        }

        if (type == QuizTruthType.ORDERING) {
            List<String> keys = extractOrderingKeys(effectivePayload);
            tb.correctItemKeysInOrder(keys);
            tb.correctAnswerDisplay(String.join(",", keys));
            return tb.build();
        }

        if (type == QuizTruthType.ASSOCIATION) {
            List<TruthPair> pairs = extractAssociationPairs(effectivePayload);
            tb.correctPairs(pairs);
            return tb.build();
        }

        throw new IllegalStateException("Truth type not implemented: " + type);
    }

    private static QuizTruthType toTruthType(QuizFormat fmt) {
        return switch (fmt) {
            case TEXT_INPUT, CLOZE, HANGMAN, WORD_PUZZLE -> QuizTruthType.TEXT;
            case MCQ -> QuizTruthType.MCQ;
            case BINARY_SWIPE -> QuizTruthType.BINARY_SWIPE;
            case ORDERING -> QuizTruthType.ORDERING;
            case ASSOCIATION -> QuizTruthType.ASSOCIATION;
        };
    }

    private List<String> extractCorrectChoiceKeys(
            QuizQuestionPayload payload,
            List<TruthAttributeValue> frozenTargetValues,
            Long targetPersonId) {

        if (payload == null || payload.getChoices() == null || payload.getChoices().isEmpty()) {
            throw new IllegalStateException("MCQ truth requires payload.choices");
        }

        List<QuizChoice> choices = payload.getChoices().stream()
                .filter(Objects::nonNull)
                .toList();

        if (choices.isEmpty()) {
            throw new IllegalStateException("MCQ truth requires non-empty choices");
        }

        if (targetPersonId != null) {
            List<String> personMatches = choices.stream()
                    .filter(c -> targetPersonId.equals(c.getPersonId()))
                    .map(QuizChoice::getId)
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .distinct()
                    .toList();

            if (!personMatches.isEmpty()) {
                return personMatches;
            }
        }

        if (frozenTargetValues != null && !frozenTargetValues.isEmpty()) {
            java.util.Set<String> truthValues = frozenTargetValues.stream()
                    .map(TruthAttributeValue::getValue)
                    .filter(Objects::nonNull)
                    .map(this::canonicalize)
                    .collect(java.util.stream.Collectors.toSet());

            List<String> valueMatches = choices.stream()
                    .filter(c -> c.getValue() != null)
                    .filter(c -> truthValues.contains(canonicalize(c.getValue())))
                    .map(QuizChoice::getId)
                    .filter(Objects::nonNull)
                    .map(String::valueOf)
                    .distinct()
                    .toList();

            if (!valueMatches.isEmpty()) {
                return valueMatches;
            }
        }

        String attempted = "Attempted matching: ";
        if (targetPersonId != null) {
            attempted += "personId=" + targetPersonId + " ";
        }
        if (frozenTargetValues != null && !frozenTargetValues.isEmpty()) {
            attempted += "frozenValues=" + frozenTargetValues.stream()
                    .map(TruthAttributeValue::getValue)
                    .toList();
        }

        throw new IllegalStateException(
                "MCQ truth extraction failed: no choices matched frozen truth. " + attempted);
    }

    private String canonicalize(String text) {
        if (text == null) {
            return "";
        }
        return java.text.Normalizer.normalize(text.trim().toLowerCase(), java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    private String firstChoiceLabel(QuizQuestionPayload payload, List<String> correctKeys) {
        if (payload == null || payload.getChoices() == null)
            return null;
        return payload.getChoices().stream()
                .filter(Objects::nonNull)
                .filter(c -> correctKeys.contains(String.valueOf(c.getId())))
                .map(c -> c.getLabel() == null ? c.getValue() : c.getLabel())
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private List<String> extractOrderingKeys(QuizQuestionPayload payload) {
        if (payload == null || payload.getItems() == null || payload.getItems().isEmpty()) {
            throw new IllegalStateException("ORDERING truth requires payload.items");
        }
        List<String> keys = payload.getItems().stream()
                .filter(Objects::nonNull)
                .map(it -> it.getPersonId() != null ? String.valueOf(it.getPersonId()) : it.getLabelId())
                .filter(Objects::nonNull)
                .toList();
        if (keys.isEmpty()) {
            throw new IllegalStateException("ORDERING truth requires non-empty item keys");
        }
        return keys;
    }

    private List<TruthPair> extractAssociationPairs(QuizQuestionPayload payload) {
        if (payload == null || payload.getItems() == null || payload.getItems().isEmpty()) {
            throw new IllegalStateException("ASSOCIATION truth requires payload.items");
        }
        List<TruthPair> pairs = payload.getItems().stream()
                .filter(Objects::nonNull)
                .map(it -> {
                    String key = it.getPersonId() != null ? String.valueOf(it.getPersonId()) : it.getLabelId();
                    return key == null ? null : new TruthPair(key, key);
                })
                .filter(Objects::nonNull)
                .toList();
        if (pairs.isEmpty()) {
            throw new IllegalStateException("ASSOCIATION truth requires non-empty correctPairs");
        }
        return pairs;
    }

    /**
     * Creates a new snapshot with the updated multi-step state.
     * Used for HANGMAN and WORD_PUZZLE formats after each step.
     */
    public static QuizQuestionSnapshot updateState(QuizQuestionSnapshot original, MultiStepState newState) {
        Objects.requireNonNull(original, "original snapshot");
        Objects.requireNonNull(newState, "newState");

        QuizQuestionSnapshot.Builder builder = QuizQuestionSnapshot.builder().from(original);

        if (newState instanceof HangmanSnapshotState hangmanState) {
            builder.hangmanState(hangmanState);
        } else if (newState instanceof WordPuzzleSnapshotState wordPuzzleState) {
            builder.wordPuzzleState(wordPuzzleState);
        } else {
            throw new IllegalArgumentException("Unknown MultiStepState type: " + newState.getClass().getName());
        }

        return builder.build();
    }
}
