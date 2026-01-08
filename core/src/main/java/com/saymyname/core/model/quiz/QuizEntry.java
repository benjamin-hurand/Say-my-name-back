package com.saymyname.core.model.quiz;

import java.util.Objects;

public class QuizEntry {
    private Long personId;
    private String storageKey;
    private String initials;

    // Constructeur par défaut
    public QuizEntry() {
    }

    // Constructeur privé utilisé par le Builder
    private QuizEntry(Builder builder) {
        this.personId = builder.personId;
        this.storageKey = builder.storageKey;
        this.initials = builder.initials;
    }

    // Getters
    public Long getPersonId() {
        return personId;
    }

    public String getStorageKey() {
        return storageKey;
    }

    public String getInitials() {
        return initials;
    }

    // Setters
    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public void setStorageKey(String storageKey) {
        this.storageKey = storageKey;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    // Builder
    public static class Builder {
        private Long personId;
        private String storageKey;
        private String initials;

        public Builder withPersonId(Long personId) {
            this.personId = personId;
            return this;
        }

        public Builder withStorageKey(String storageKey) {
            this.storageKey = storageKey;
            return this;
        }

        public Builder withInitials(String initials) {
            this.initials = initials;
            return this;
        }

        public QuizEntry build() {
            return new QuizEntry(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof QuizEntry))
            return false;
        QuizEntry that = (QuizEntry) o;
        return Objects.equals(personId, that.personId) &&
                Objects.equals(storageKey, that.storageKey) &&
                Objects.equals(initials, that.initials);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personId, storageKey, initials);
    }

    @Override
    public String toString() {
        return "QuizEntry{" +
                "personId=" + personId +
                ", storageKey=" + storageKey +
                ", initials='" + initials + '\'' +
                '}';
    }
}
