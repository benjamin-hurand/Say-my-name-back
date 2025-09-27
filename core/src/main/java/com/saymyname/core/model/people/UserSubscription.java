package com.saymyname.core.model.people;

import java.time.Instant;
import java.util.Objects;

/**
 * Modèle métier d'un abonnement utilisateur -> personne.
 * - Un enregistrement signifie que l'utilisateur "suit" la personne.
 * - Egalité/HashCode basés uniquement sur (userId, personId).
 */
public class UserSubscription {

    private Long userId;
    private Long personId;
    /** Timestamp indicatif (optionnel à l'insert, fourni par la DB). */
    private Instant createdAt;

    public UserSubscription() {
    }

    private UserSubscription(Builder builder) {
        this.userId = builder.userId;
        this.personId = builder.personId;
        this.createdAt = builder.createdAt;
    }

    /** Factory pratique. */
    public static UserSubscription of(Long userId, Long personId) {
        return new Builder()
                .withUserId(userId)
                .withPersonId(personId)
                .build();
    }

    // Getters
    public Long getUserId() {
        return userId;
    }

    public Long getPersonId() {
        return personId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    // Builder
    public static class Builder {
        private Long userId;
        private Long personId;
        private Instant createdAt;

        public Builder withUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder withPersonId(Long personId) {
            this.personId = personId;
            return this;
        }

        public Builder withCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public UserSubscription build() {
            return new UserSubscription(this);
        }
    }

    // Egalité/HashCode : uniquement la clé logique (userId, personId)
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserSubscription that))
            return false;
        return Objects.equals(userId, that.userId)
                && Objects.equals(personId, that.personId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, personId);
    }

    @Override
    public String toString() {
        return "UserSubscription{" +
                "userId=" + userId +
                ", personId=" + personId +
                ", createdAt=" + createdAt +
                '}';
    }
}
