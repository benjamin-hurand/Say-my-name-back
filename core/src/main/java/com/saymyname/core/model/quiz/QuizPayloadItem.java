// src/main/java/com/saymyname/core/model/quiz/QuizPayloadItem.java
package com.saymyname.core.model.quiz;

import java.util.Objects;

public class QuizPayloadItem {

    private Long personId;
    private String storageKey; // important: core stores key, webapp maps to photoUrl
    private String labelId; // i18n key or internal label id

    public QuizPayloadItem() {
    }

    private QuizPayloadItem(Builder b) {
        this.personId = b.personId;
        this.storageKey = b.storageKey;
        this.labelId = b.labelId;
    }

    public Long getPersonId() {
        return personId;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getLabelId() {
        return labelId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public void setLabelId(String labelId) {
        this.labelId = labelId;
    }

    public static class Builder {
        private Long personId;
        private String storageKey;
        private String labelId;

        public Builder withPersonId(Long v) {
            this.personId = v;
            return this;
        }

        public Builder withStorageKey(String v) {
            this.storageKey = v;
            return this;
        }

        public Builder withLabelId(String v) {
            this.labelId = v;
            return this;
        }

        public QuizPayloadItem build() {
            return new QuizPayloadItem(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof QuizPayloadItem))
            return false;
        QuizPayloadItem that = (QuizPayloadItem) o;
        return Objects.equals(personId, that.personId)
                && Objects.equals(storageKey, that.storageKey)
                && Objects.equals(labelId, that.labelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personId, storageKey, labelId);
    }
}
