package com.saymyname.core.model.game;

import java.util.Objects;

public class QuizEntry {
    private Long personId;
    private String photoUrl;
    private String initials;

    // Constructeur par défaut
    public QuizEntry() {}

    // Constructeur privé utilisé par le Builder
    private QuizEntry(Builder builder) {
        this.personId = builder.personId;
        this.photoUrl = builder.photoUrl;
        this.initials = builder.initials;
    }

    // Getters
    public Long getPersonId() {
        return personId;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getInitials() {
        return initials;
    }

    // Setters
    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setInitials(String initials) {
        this.initials = initials;
    }

    // Builder
    public static class Builder {
        private Long personId;
        private String photoUrl;
        private String initials;

        public Builder withPersonId(Long personId) {
            this.personId = personId;
            return this;
        }

        public Builder withPhotoUrl(String photoUrl) {
            this.photoUrl = photoUrl;
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
        if (this == o) return true;
        if (!(o instanceof QuizEntry)) return false;
        QuizEntry that = (QuizEntry) o;
        return Objects.equals(personId, that.personId) &&
               Objects.equals(photoUrl, that.photoUrl) &&
               Objects.equals(initials, that.initials);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personId, photoUrl, initials);
    }

    @Override
    public String toString() {
        return "QuizEntry{" +
                "personId=" + personId +
                ", photoUrl=" + photoUrl +
                ", initials='" + initials + '\'' +
                '}';
    }
}
