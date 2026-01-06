package com.saymyname.persistence.entity.organization.leaderboard;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.*;

import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.jpa.UuidBytesConverter;
import com.saymyname.persistence.multitenancy.BaseOrgScoped;

@Entity
@Table(name = "xp_events", indexes = {
        @Index(name = "idx_xp_event_org_user_created", columnList = "organization_id, user_id, created_at, id"),
        @Index(name = "idx_xp_event_org_created", columnList = "organization_id, created_at, id"),
        @Index(name = "idx_xp_event_source", columnList = "source_type, source_id"),
        @Index(name = "idx_xp_event_key", columnList = "event_key")
})
public class XpEventEntity extends BaseOrgScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    /**
     * ✅ Identifiant unique d’évènement (idempotence).
     * UNIQUE(organization_id, event_id) côté DB.
     */
    @Convert(converter = UuidBytesConverter.class)
    @Column(name = "event_id", columnDefinition = "BINARY(16)", nullable = false, updatable = false)
    private UUID eventId;

    /**
     * ✅ Clé fonctionnelle stable (QUIZ_CORRECT, QUIZ_STREAK, etc.)
     */
    @Column(name = "event_key", nullable = false, length = 64)
    private String eventKey;

    /**
     * Type de source métier (QUIZ, COURSE, SYSTEM, etc.)
     * Si ta DB est NOT NULL : mets "SYSTEM" par défaut dans le service.
     */
    @Column(name = "source_type", length = 32, nullable = false)
    private String sourceType;

    /**
     * Id de la source métier (quizId, courseId, etc.)
     */
    @Column(name = "source_id")
    private Long sourceId;

    /**
     * ✅ Variation d’XP (+10, -5, etc.)
     */
    @Column(name = "delta_xp", nullable = false)
    private int deltaXp;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public XpEventEntity() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null)
            this.createdAt = LocalDateTime.now();
        if (this.eventId == null)
            this.eventId = UUID.randomUUID(); // ✅ safe default
        if (this.sourceType == null)
            this.sourceType = "SYSTEM"; // ✅ si DB NOT NULL
        if (this.eventKey == null)
            this.eventKey = "UNKNOWN"; // ✅ si DB NOT NULL
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public UUID getEventId() {
        return eventId;
    }

    public void setEventId(UUID eventId) {
        this.eventId = eventId;
    }

    public String getEventKey() {
        return eventKey;
    }

    public void setEventKey(String eventKey) {
        this.eventKey = eventKey;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public void setSourceId(Long sourceId) {
        this.sourceId = sourceId;
    }

    public int getDeltaXp() {
        return deltaXp;
    }

    public void setDeltaXp(int deltaXp) {
        this.deltaXp = deltaXp;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof XpEventEntity))
            return false;
        XpEventEntity that = (XpEventEntity) o;

        // Identité DB si id présent
        if (this.id != null && that.id != null) {
            return Objects.equals(this.id, that.id);
        }

        // fallback sans toucher proxies
        Long thisUserId = (this.user != null ? this.user.getId() : null);
        Long thatUserId = (that.user != null ? that.user.getId() : null);

        return deltaXp == that.deltaXp
                && Objects.equals(getOrganizationId(), that.getOrganizationId())
                && Objects.equals(thisUserId, thatUserId)
                && Objects.equals(eventId, that.eventId)
                && Objects.equals(eventKey, that.eventKey)
                && Objects.equals(sourceType, that.sourceType)
                && Objects.equals(sourceId, that.sourceId)
                && Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        if (id != null)
            return Objects.hash(id);
        Long userId = (user != null ? user.getId() : null);
        return Objects.hash(getOrganizationId(), userId, eventId, eventKey, sourceType, sourceId, deltaXp, createdAt);
    }

    @Override
    public String toString() {
        return "XpEventEntity{" +
                "id=" + id +
                ", orgId=" + getOrganizationId() +
                ", user=" + (user != null ? user.getId() : null) +
                ", eventId=" + eventId +
                ", eventKey='" + eventKey + '\'' +
                ", sourceType='" + sourceType + '\'' +
                ", sourceId=" + sourceId +
                ", deltaXp=" + deltaXp +
                ", createdAt=" + createdAt +
                '}';
    }
}
