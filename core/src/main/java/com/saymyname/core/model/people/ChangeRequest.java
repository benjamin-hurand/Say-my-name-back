package com.saymyname.core.model.people;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.ChangeStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Enveloppe d’une demande de changement.
 * - Métadonnées (person, requester, statut, audit...)
 * - Motif global (requestReason)
 * - Liste d’items (la vérité métier)
 */
public class ChangeRequest {

    private Long id;

    private Person person; // cible
    private User requester; // auteur
    private Attribute attribute;

    /** Motif global saisi par le demandeur (obligatoire au niveau API/DB) */
    private String requestReason;

    private ChangeStatus status; // PENDING, APPROVED, REJECTED, CANCELED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private User resolvedBy; // qui a résolu l’enveloppe
    private LocalDateTime resolvedAt;
    private String resolutionComment;

    /** Items associés (CREATE/UPDATE/DELETE) */
    private List<ChangeRequestItem> items = Collections.emptyList();

    public ChangeRequest() {
        // rien de spécial, items = emptyList par défaut
    }

    private ChangeRequest(Builder b) {
        this.id = b.id;
        this.person = b.person;
        this.requester = b.requester;
        this.attribute = b.attribute;
        this.requestReason = b.requestReason;
        this.status = b.status;
        this.createdAt = b.createdAt;
        this.updatedAt = b.updatedAt;
        this.resolvedBy = b.resolvedBy;
        this.resolvedAt = b.resolvedAt;
        this.resolutionComment = b.resolutionComment;
        setItems(b.items);
    }

    // --- Getters ---
    public Long getId() {
        return id;
    }

    public Person getPerson() {
        return person;
    }

    public User getRequester() {
        return requester;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    /** Motif global de la demande */
    public String getRequestReason() {
        return requestReason;
    }

    public ChangeStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public User getResolvedBy() {
        return resolvedBy;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public String getResolutionComment() {
        return resolutionComment;
    }

    public List<ChangeRequestItem> getItems() {
        return items;
    }

    // --- Setters ---
    public void setId(Long id) {
        this.id = id;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setRequester(User requester) {
        this.requester = requester;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public void setRequestReason(String requestReason) {
        this.requestReason = requestReason;
    }

    public void setStatus(ChangeStatus status) {
        this.status = status;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setResolvedBy(User resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public void setResolutionComment(String resolutionComment) {
        this.resolutionComment = resolutionComment;
    }

    public void setItems(List<ChangeRequestItem> items) {
        this.items = (items != null ? new ArrayList<>(items) : Collections.emptyList());
    }

    // --- Builder ---
    public static class Builder {
        private Long id;
        private Person person;
        private User requester;
        private Attribute attribute;
        private String requestReason;
        private ChangeStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private User resolvedBy;
        private LocalDateTime resolvedAt;
        private String resolutionComment;
        private List<ChangeRequestItem> items;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withPerson(Person person) {
            this.person = person;
            return this;
        }

        public Builder withRequester(User requester) {
            this.requester = requester;
            return this;
        }

        public Builder withAttribute(Attribute attribute) {
            this.attribute = attribute;
            return this;
        }

        /** Motif global fourni par le demandeur */
        public Builder withRequestReason(String requestReason) {
            this.requestReason = requestReason;
            return this;
        }

        public Builder withStatus(ChangeStatus status) {
            this.status = status;
            return this;
        }

        public Builder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder withResolvedBy(User resolvedBy) {
            this.resolvedBy = resolvedBy;
            return this;
        }

        public Builder withResolvedAt(LocalDateTime resolvedAt) {
            this.resolvedAt = resolvedAt;
            return this;
        }

        public Builder withResolutionComment(String resolutionComment) {
            this.resolutionComment = resolutionComment;
            return this;
        }

        public Builder withItems(List<ChangeRequestItem> items) {
            this.items = items;
            return this;
        }

        public ChangeRequest build() {
            return new ChangeRequest(this);
        }
    }

    // --- equals / hashCode (id only) ---
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ChangeRequest))
            return false;
        ChangeRequest that = (ChangeRequest) o;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return "ChangeRequest{" +
                "id=" + id +
                ", personId=" + (person != null ? person.getId() : null) +
                ", requesterId=" + (requester != null ? requester.getId() : null) +
                ", attributeId=" + (attribute != null ? attribute.getId() : null) +
                ", status=" + status +
                ", itemsCount=" + (items != null ? items.size() : 0) +
                '}';
    }
}
