// core/model/quiz/candidate/CandidateSample.java
package com.saymyname.core.model.quiz.candidate;

import java.util.List;
import java.util.Objects;

/**
 * Sampled candidates with minimal payload (no full Person).
 * Contains only fields needed for question building.
 *
 * Includes a designated target person (subject of the question)
 * and optionally other candidates for distractors (MCQ, Association, etc.)
 */
public record CandidateSample(
        List<PayloadItem> items,
        Long targetPersonId,
        String targetStorageKey) {

    public CandidateSample {
        items = (items == null) ? List.of() : List.copyOf(items);
    }

    /**
     * Create a sample with a single target person.
     */
    public static CandidateSample ofSingle(PayloadItem target) {
        Objects.requireNonNull(target, "target");
        return new CandidateSample(
                List.of(target),
                target.personId(),
                target.photoStorageKey());
    }

    /**
     * Create a sample with multiple items and a designated target.
     */
    public static CandidateSample of(List<PayloadItem> items, Long targetPersonId) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("items must not be empty");
        }
        Objects.requireNonNull(targetPersonId, "targetPersonId");

        String targetStorageKey = items.stream()
                .filter(i -> targetPersonId.equals(i.personId()))
                .findFirst()
                .map(PayloadItem::photoStorageKey)
                .orElse(null);

        return new CandidateSample(items, targetPersonId, targetStorageKey);
    }

    /**
     * Get all person IDs in this sample.
     */
    public List<Long> personIds() {
        return items.stream()
                .map(PayloadItem::personId)
                .toList();
    }

    /**
     * Get items excluding the target person (for distractor building).
     */
    public List<PayloadItem> distractors() {
        if (targetPersonId == null) {
            return items;
        }
        return items.stream()
                .filter(i -> !targetPersonId.equals(i.personId()))
                .toList();
    }

    /**
     * Get the target item.
     */
    public PayloadItem target() {
        if (targetPersonId == null) {
            return items.isEmpty() ? null : items.get(0);
        }
        return items.stream()
                .filter(i -> targetPersonId.equals(i.personId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Number of items in the sample.
     */
    public int size() {
        return items.size();
    }

    /**
     * Check if sample is empty.
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }
}
