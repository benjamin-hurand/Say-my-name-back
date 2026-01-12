// src/main/java/com/saymyname/service/quiz/QuizQuestionSnapshotFactory.java
package com.saymyname.service.quiz;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.enums.quiz.snapshot.QuizTruthType;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.core.model.quiz.QuizChoice;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.QuizQuestionPayload;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionTruth;
import com.saymyname.core.model.quiz.snapshot.QuizTruthRules;
import com.saymyname.core.model.quiz.snapshot.TruthAttributeValue;
import com.saymyname.core.model.quiz.snapshot.TruthPair;
import com.saymyname.persistence.dao.PersonAttributeDao;

/**
 * Construit un snapshot auto-suffisant (truth + payload) sans dépendance au
 * token.
 */
@Component
public class QuizQuestionSnapshotFactory {

    private static final int SNAPSHOT_SCHEMA_VERSION = 4;
    private static final String GENERATOR_VERSION = "quiz-pipeline-v1";
    private static final String NORMALIZER_VERSION = "normalizer-v1";

    private final PersonAttributeDao personAttributeDao;

    public QuizQuestionSnapshotFactory(PersonAttributeDao personAttributeDao) {
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

        List<PersonAttribute> personAttrs = personAttributeDao.findAttributesByPersonId(q.getPersonId());

        List<TruthAttributeValue> out = new ArrayList<>();
        for (Long attrId : q.getTargetAttributeIds()) {
            if (attrId == null)
                continue;

            List<PersonAttribute> matches = personAttrs.stream()
                    .filter(pa -> pa != null && pa.getAttribute() != null && attrId.equals(pa.getAttribute().getId()))
                    .toList();

            for (PersonAttribute pa : matches) {
                if (pa.getValue() == null || pa.getValue().isBlank())
                    continue;

                out.add(new TruthAttributeValue.Builder()
                        .withAttributeId(attrId)
                        .withValue(pa.getValue())
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

    /**
     * Construit un snapshot à partir d'une question runtime + truth figée.
     * Le snapshot ne dépend pas du token.
     */
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

        return new QuizQuestionSnapshot.Builder()
                .withSnapshotSchemaVersion(SNAPSHOT_SCHEMA_VERSION)
                .withGeneratorVersion(GENERATOR_VERSION)
                .withNormalizerVersion(NORMALIZER_VERSION)

                .withFormat(q.getFormat())
                .withContext(q.getContext())

                // auditable spec
                .withGameModeId(q.getGameModeId())
                .withTargetAttributeIds(new ArrayList<>(q.getTargetAttributeIds()))
                .withOperator(q.getOperator())
                .withPersonId(q.getPersonId())
                .withStorageKey(q.getStorageKey())

                // renderable parts
                .withDisplay(q.getDisplay())
                .withHints(q.getHints())
                .withPayload(q.getPayload())
                .withFollowUp(q.getFollowUp())
                .withTimed(timed)
                .withTimeLimitMs(timeLimitMs)
                .withReasonCode(q.getReasonCode())
                .withReasonDetailsJson(q.getReasonDetailsJson())

                // truth
                .withTruth(truth)
                .withTargetPersonIds(resolvedTargets)
                .build();
    }

    private QuizQuestionTruth buildTruth(QuizQuestion q, List<TruthAttributeValue> frozenTargetValues) {

        QuizTruthRules rules = new QuizTruthRules.Builder()
                .withOperator(q.getOperator())
                .build();

        QuizFormat effectiveFormat = q.getFormat();
        QuizQuestionPayload effectivePayload = q.getPayload();

        QuizTruthType type = toTruthType(effectiveFormat);

        QuizQuestionTruth.Builder tb = new QuizQuestionTruth.Builder()
                .withType(type)
                .withRules(rules);

        if (type == QuizTruthType.TEXT) {
            if (frozenTargetValues == null || frozenTargetValues.isEmpty()) {
                throw new IllegalStateException("TEXT truth requires frozenTargetValues");
            }
            tb.withTargetAttributeValues(new ArrayList<>(frozenTargetValues));

            String display = frozenTargetValues.stream()
                    .map(TruthAttributeValue::getValue)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isBlank())
                    .reduce((a, b) -> a + " " + b)
                    .orElse(null);

            tb.withCorrectAnswerDisplay(display);
            return tb.build();
        }

        if (type == QuizTruthType.MCQ) {
            // MCQ truth extraction now uses matching instead of deprecated correct flag
            List<String> correctKeys = extractCorrectChoiceKeys(
                    effectivePayload,
                    frozenTargetValues,
                    q.getPersonId());
            tb.withCorrectChoiceKeys(correctKeys);
            tb.withCorrectAnswerDisplay(firstChoiceLabel(effectivePayload, correctKeys));
            return tb.build();
        }

        if (type == QuizTruthType.BINARY_SWIPE) {
            tb.withCorrectSwipeRight(Boolean.TRUE);
            tb.withCorrectAnswerDisplay("true");
            return tb.build();
        }

        if (type == QuizTruthType.ORDERING) {
            List<String> keys = extractOrderingKeys(effectivePayload);
            tb.withCorrectItemKeysInOrder(keys);
            tb.withCorrectAnswerDisplay(String.join(",", keys));
            return tb.build();
        }

        if (type == QuizTruthType.ASSOCIATION) {
            List<TruthPair> pairs = extractAssociationPairs(effectivePayload);
            tb.withCorrectPairs(pairs);
            return tb.build();
        }

        throw new IllegalStateException("Truth type not implemented: " + type);
    }

    private static QuizTruthType toTruthType(QuizFormat fmt) {
        return switch (fmt) {
            case TEXT_INPUT, CLOZE, HANGMAN -> QuizTruthType.TEXT;
            case MCQ -> QuizTruthType.MCQ;
            case BINARY_SWIPE -> QuizTruthType.BINARY_SWIPE;
            case ORDERING -> QuizTruthType.ORDERING;
            case ASSOCIATION -> QuizTruthType.ASSOCIATION;
        };
    }

    /**
     * Extract correct choice keys by matching against frozen truth.
     *
     * Strategy:
     * 1. Try personId matching (for photo MCQs: "Who is this person?")
     * 2. Try value matching (for text MCQs: "What is X's last name?")
     *
     * @param payload The question payload with choices
     * @param frozenTargetValues The frozen truth values from EAV
     * @param targetPersonId The target person ID (for photo MCQs)
     * @return List of correct choice IDs
     */
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

        // Branch 1: Match by personId (photo MCQs)
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

        // Branch 2: Match by value (text MCQs)
        if (frozenTargetValues != null && !frozenTargetValues.isEmpty()) {
            // Build set of canonicalized truth values
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

        // No matches found - provide actionable error
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

    /**
     * Canonicalize text for case-insensitive, trim, diacritic-normalized comparison.
     */
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
                .map(it -> it.getLabelId() != null ? it.getLabelId()
                        : (it.getPersonId() != null ? String.valueOf(it.getPersonId()) : null))
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
                    String key = it.getLabelId() != null ? it.getLabelId()
                            : (it.getPersonId() != null ? String.valueOf(it.getPersonId()) : null);
                    return key == null ? null : new TruthPair(key, key);
                })
                .filter(Objects::nonNull)
                .toList();
        if (pairs.isEmpty()) {
            throw new IllegalStateException("ASSOCIATION truth requires non-empty correctPairs");
        }
        return pairs;
    }
}
