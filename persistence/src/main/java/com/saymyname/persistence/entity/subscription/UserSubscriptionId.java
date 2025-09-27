package com.saymyname.persistence.entity.subscription;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Clé composée (user_id, person_id) pour user_subscriptions. */
@Embeddable
public class UserSubscriptionId {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "person_id", nullable = false)
    private Long personId;

    public UserSubscriptionId() {
    }

    public UserSubscriptionId(Long userId, Long personId) {
        this.userId = userId;
        this.personId = personId;
    }

    // Getters
    public Long getUserId() {
        return userId;
    }

    public Long getPersonId() {
        return personId;
    }

    // Setters
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserSubscriptionId that))
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
        return "UserSubscriptionId{" +
                "userId=" + userId +
                ", personId=" + personId +
                '}';
    }
}
