// src/main/java/com/saymyname/core/model/quiz/QuizDisplayItem.java
package com.saymyname.core.model.quiz;

import java.util.Objects;

/**
 * Unified display model for quiz content that can be rendered as:
 * - Text only
 * - Photo only (via storageKey)
 * - Person reference (for avatar + name lookup)
 * - Any combination of above
 *
 * Used by: MCQ choices, BINARY_SWIPE proposition, ASSOCIATION items, ORDERING items
 *
 * Design principles:
 * - Core stores only storageKey (backend key)
 * - Webapp layer resolves to photoUrl via PhotoUrlResolver
 * - All fields nullable for flexibility
 * - Immutable via builder pattern
 *
 * CRITICAL SECURITY NOTE:
 * This model is for DISPLAY ONLY. It contains NO truth/answer key data.
 * Truth is stored ONLY in QuizQuestionSnapshot.truth (backend-only).
 * Never add 'correct', 'expected', or any validation fields to this class.
 */
public class QuizDisplayItem {

    /**
     * Stable ID for frontend reconciliation (e.g., React key).
     * Not a database ID - just needs to be unique within the question.
     */
    private Long id;

    /**
     * Display text (can be null for photo-only items).
     * Examples: "Marie", "Paris", "Capital city"
     */
    private String label;

    /**
     * Canonical value for backend processing (can differ from label).
     * Examples: "marie dupont", "paris", "france"
     * Used for normalization and logging, NOT for validation.
     * Validation happens in plugins against snapshot.truth.
     */
    private String value;

    /**
     * Backend storage key for photo (null if no photo).
     * Webapp maps this to photoUrl via PhotoUrlResolver.
     */
    private String storageKey;

    /**
     * Person reference ID (null if not person-based).
     * Used for:
     * - MCQ choices: when choice represents a person
     * - ASSOCIATION: pairing person photos with attributes
     * - ORDERING: ordering people by some criteria
     */
    private Long personId;

    /**
     * Optional role/type hint for frontend layout.
     * Examples: "left", "right", "source", "target"
     * Used by ASSOCIATION to distinguish left/right columns.
     */
    private String role;

    public QuizDisplayItem() {
    }

    protected QuizDisplayItem(Builder<?> b) {
        this.id = b.id;
        this.label = b.label;
        this.value = b.value;
        this.storageKey = b.storageKey;
        this.personId = b.personId;
        this.role = b.role;
    }

    public Long getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getValue() {
        return value;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public Long getPersonId() {
        return personId;
    }

    public String getRole() {
        return role;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @SuppressWarnings("unchecked")
    public static class Builder<T extends Builder<T>> {
        private Long id;
        private String label;
        private String value;
        private String storageKey;
        private Long personId;
        private String role;

        public T withId(Long v) {
            this.id = v;
            return (T) this;
        }

        public T withLabel(String v) {
            this.label = v;
            return (T) this;
        }

        public T withValue(String v) {
            this.value = v;
            return (T) this;
        }

        public T withStorageKey(String v) {
            this.storageKey = v;
            return (T) this;
        }

        public T withPersonId(Long v) {
            this.personId = v;
            return (T) this;
        }

        public T withRole(String v) {
            this.role = v;
            return (T) this;
        }

        public QuizDisplayItem build() {
            return new QuizDisplayItem(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof QuizDisplayItem))
            return false;
        QuizDisplayItem that = (QuizDisplayItem) o;
        return Objects.equals(id, that.id)
                && Objects.equals(label, that.label)
                && Objects.equals(value, that.value)
                && Objects.equals(storageKey, that.storageKey)
                && Objects.equals(personId, that.personId)
                && Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, value, storageKey, personId, role);
    }

    @Override
    public String toString() {
        return "QuizDisplayItem{" +
                "id=" + id +
                ", label='" + label + '\'' +
                ", value='" + value + '\'' +
                ", storageKey='" + storageKey + '\'' +
                ", personId=" + personId +
                ", role='" + role + '\'' +
                '}';
    }
}
