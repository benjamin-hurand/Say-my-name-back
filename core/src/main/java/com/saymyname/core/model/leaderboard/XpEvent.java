// src/main/java/com/saymyname/core/model/leaderboard/XpEvent.java
package com.saymyname.core.model.leaderboard;

import com.saymyname.core.model.auth.User;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class XpEvent {

    private Long id;
    private User user;

    private UUID eventId; // ✅ idempotence stable
    private String eventKey; // ✅ type fonctionnel (QUIZ_CORRECT, etc.)

    private String sourceType;
    private Long sourceId;

    private int deltaXp; // ✅ explicite
    private LocalDateTime createdAt;

    public XpEvent() {
    }

    private XpEvent(Builder b) {
        this.id = b.id;
        this.user = b.user;
        this.eventId = b.eventId;
        this.eventKey = b.eventKey;
        this.sourceType = b.sourceType;
        this.sourceId = b.sourceId;
        this.deltaXp = b.deltaXp;
        this.createdAt = b.createdAt;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public UUID getEventId() {
        return eventId;
    }

    public String getEventKey() {
        return eventKey;
    }

    public String getSourceType() {
        return sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public int getDeltaXp() {
        return deltaXp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public void setDeltaXp(int deltaXp) {
        this.deltaXp = deltaXp;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public static class Builder {
        private Long id;
        private User user;
        private UUID eventId;
        private String eventKey;
        private String sourceType;
        private Long sourceId;
        private int deltaXp;
        private LocalDateTime createdAt;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withUser(User user) {
            this.user = user;
            return this;
        }

        public Builder withEventId(UUID eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder withEventKey(String eventKey) {
            this.eventKey = eventKey;
            return this;
        }

        public Builder withSourceType(String sourceType) {
            this.sourceType = sourceType;
            return this;
        }

        public Builder withSourceId(Long sourceId) {
            this.sourceId = sourceId;
            return this;
        }

        public Builder withDeltaXp(int deltaXp) {
            this.deltaXp = deltaXp;
            return this;
        }

        public Builder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public XpEvent build() {
            return new XpEvent(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof XpEvent))
            return false;
        XpEvent that = (XpEvent) o;
        return deltaXp == that.deltaXp
                && Objects.equals(id, that.id)
                && Objects.equals(user, that.user)
                && Objects.equals(eventId, that.eventId)
                && Objects.equals(eventKey, that.eventKey)
                && Objects.equals(sourceType, that.sourceType)
                && Objects.equals(sourceId, that.sourceId)
                && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, eventId, eventKey, sourceType, sourceId, deltaXp, createdAt);
    }
}
