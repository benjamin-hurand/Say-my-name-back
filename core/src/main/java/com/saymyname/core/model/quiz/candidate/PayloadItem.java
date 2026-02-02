// core/model/quiz/candidate/PayloadItem.java
package com.saymyname.core.model.quiz.candidate;

import java.util.Map;
import java.util.Objects;

/**
 * Minimal person data for quiz building.
 * NOT a full Person entity - just what plugins need.
 *
 * Contains only:
 * - personId (required)
 * - photoStorageKey (nullable if no approved photo)
 * - attributeValues (gameMode attributes only, keyed by attributeId)
 */
public record PayloadItem(
        Long personId,
        String photoStorageKey,
        Map<Long, String> attributeValues) {

    public PayloadItem {
        Objects.requireNonNull(personId, "personId is required");
        attributeValues = (attributeValues == null) ? Map.of() : Map.copyOf(attributeValues);
    }

    /**
     * Convenience constructor without photo.
     */
    public PayloadItem(Long personId, Map<Long, String> attributeValues) {
        this(personId, null, attributeValues);
    }

    /**
     * Get attribute value by attributeId.
     *
     * @param attributeId the attribute ID
     * @return the value or null if not present
     */
    public String attributeValue(Long attributeId) {
        return attributeValues.get(attributeId);
    }

    /**
     * Check if this item has an approved photo.
     */
    public boolean hasPhoto() {
        return photoStorageKey != null && !photoStorageKey.isBlank();
    }
}
