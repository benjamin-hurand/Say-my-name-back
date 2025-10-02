package com.saymyname.persistence.entity.organization.subscription;

import java.time.Instant;
import java.util.Objects;

import com.saymyname.persistence.multitenancy.BaseOrgScoped;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * Entité "user_subscriptions" minimale et performante.
 * - PK composite via @EmbeddedId (user_id, person_id)
 * - created_at géré par la DB (DEFAULT CURRENT_TIMESTAMP)
 * - Pas d'associations @ManyToOne pour éviter des charges/lazy involontaires
 */
@Entity
@Table(name = "user_subscriptions", indexes = {
        @Index(name = "idx_us_person", columnList = "person_id")
})
public class UserSubscriptionEntity extends BaseOrgScoped {

    @EmbeddedId
    private UserSubscriptionId id;

    /** Valeur posée par la DB : DEFAULT CURRENT_TIMESTAMP */
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private Instant createdAt;

    public UserSubscriptionEntity() {
    }

    public UserSubscriptionEntity(UserSubscriptionId id, Instant createdAt) {
        this.id = id;
        this.createdAt = createdAt;
    }

    public UserSubscriptionEntity(UserSubscriptionId id) {
        this(id, null);
    }

    // Getters
    public UserSubscriptionId getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    // Setters
    public void setId(UserSubscriptionId id) {
        this.id = id;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /** Helpers pratiques d'accès à la clé */
    public Long getUserId() {
        return id != null ? id.getUserId() : null;
    }

    public Long getPersonId() {
        return id != null ? id.getPersonId() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserSubscriptionEntity that))
            return false;
        return Objects.equals(id, that.id)
                && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, createdAt);
    }

    @Override
    public String toString() {
        return "UserSubscriptionEntity{" +
                "id=" + id +
                ", createdAt=" + createdAt +
                '}';
    }
}
