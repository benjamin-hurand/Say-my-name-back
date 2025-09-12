package com.saymyname.persistence.entity;

import com.saymyname.core.model.enums.ChangeStatus;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "change_requests")
public class ChangeRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    /** Porteur fonctionnel principal (liste par personne, etc.) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    private PersonEntity person;

    /** Membre qui a soumis la demande (parent) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id", nullable = false)
    private UserEntity requester;

    /** Attribut ciblé par l'enveloppe (obligatoire) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_id", nullable = false)
    private AttributeEntity attribute;

    /** Motif global de la demande (obligatoire) */
    @Column(name = "request_reason", nullable = false, length = 1024)
    private String requestReason;

    /** Statut de l'enveloppe (généralement calé sur l’agrégation des items) */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private ChangeStatus status = ChangeStatus.PENDING;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Qui a “résolu” l’enveloppe (souvent un admin) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by")
    private UserEntity resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolution_comment", length = 500)
    private String resolutionComment;

    /** Nouvel axe: tous les items (la vérité métier) */
    @OneToMany(mappedBy = "changeRequest", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChangeRequestItemEntity> items = new ArrayList<>();

    // --- Constructeurs ---
    public ChangeRequestEntity() {
    }

    public ChangeRequestEntity(
            Long id,
            PersonEntity person,
            UserEntity requester,
            AttributeEntity attribute,
            String requestReason,
            ChangeStatus status,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            UserEntity resolvedBy,
            LocalDateTime resolvedAt,
            String resolutionComment,
            List<ChangeRequestItemEntity> items) {
        this.id = id;
        this.person = person;
        this.requester = requester;
        this.attribute = attribute;
        this.requestReason = requestReason;
        this.status = (status != null ? status : ChangeStatus.PENDING);
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.resolvedBy = resolvedBy;
        this.resolvedAt = resolvedAt;
        this.resolutionComment = resolutionComment;
        this.items = (items != null ? items : new ArrayList<>());
    }

    // --- Lifecycle ---
    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // --- Helpers bidirectionnels ---
    public void addItem(ChangeRequestItemEntity item) {
        if (item == null)
            return;
        items.add(item);
        item.setChangeRequest(this);
    }

    public void removeItem(ChangeRequestItemEntity item) {
        if (item == null)
            return;
        items.remove(item);
        item.setChangeRequest(null);
    }

    // --- Getters / Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PersonEntity getPerson() {
        return person;
    }

    public void setPerson(PersonEntity person) {
        this.person = person;
    }

    public UserEntity getRequester() {
        return requester;
    }

    public void setRequester(UserEntity requester) {
        this.requester = requester;
    }

    public AttributeEntity getAttribute() {
        return attribute;
    }

    public void setAttribute(AttributeEntity attribute) {
        this.attribute = attribute;
    }

    public String getRequestReason() {
        return requestReason;
    }

    public void setRequestReason(String requestReason) {
        this.requestReason = requestReason;
    }

    public ChangeStatus getStatus() {
        return status;
    }

    public void setStatus(ChangeStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UserEntity getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(UserEntity resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolutionComment() {
        return resolutionComment;
    }

    public void setResolutionComment(String resolutionComment) {
        this.resolutionComment = resolutionComment;
    }

    public List<ChangeRequestItemEntity> getItems() {
        return items;
    }

    public void setItems(List<ChangeRequestItemEntity> items) {
        this.items = (items != null ? items : new ArrayList<>());
    }

    // --- equals / hashCode sur id ---
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ChangeRequestEntity))
            return false;
        ChangeRequestEntity that = (ChangeRequestEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    // toString limité
    @Override
    public String toString() {
        return "ChangeRequestEntity{" +
                "id=" + id +
                ", personId=" + (person != null ? person.getId() : null) +
                ", requesterId=" + (requester != null ? requester.getId() : null) +
                ", attributeId=" + (attribute != null ? attribute.getId() : null) +
                ", status=" + status +
                ", itemsCount=" + (items != null ? items.size() : 0) +
                '}';
    }
}
