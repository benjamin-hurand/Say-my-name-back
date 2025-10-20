// src/main/java/com/saymyname/core/model/people/ChangeRequestItem.java
package com.saymyname.core.model.people;

import com.saymyname.core.model.enums.ChangeAction;
import com.saymyname.core.model.enums.ChangeItemResolutionStatus;

import java.util.Objects;

/**
 * Ligne métier d’une demande (CREATE/UPDATE/DELETE).
 * - CREATE : personAttribute = null, proposedValue requis
 * - UPDATE : personAttribute requis, proposedValue requis
 * - DELETE : personAttribute requis, proposedValue null
 *
 * Métadonnées de résolution (status/comment) sont portées par l'ITEM.
 * Les champs "qui/quand" (resolvedBy/At) restent sur l'ENVELOPPE
 * (ChangeRequest).
 */
public class ChangeRequestItem {

    private Long id;

    /** Enveloppe parente */
    private ChangeRequest changeRequest;

    /** Action demandée */
    private ChangeAction action;

    /** Cible pour UPDATE/DELETE (null sinon) */
    private PersonAttribute personAttribute;

    /** Valeur proposée (null pour DELETE) */
    private String proposedValue;

    /** Statut de résolution (par item) — PENDING par défaut */
    private ChangeItemResolutionStatus resolutionStatus = ChangeItemResolutionStatus.PENDING;

    /** Commentaire de résolution (facultatif, utile surtout pour REJECTED) */
    private String resolutionComment;

    public ChangeRequestItem() {
    }

    private ChangeRequestItem(Builder b) {
        this.id = b.id;
        this.changeRequest = b.changeRequest;
        this.action = b.action;
        this.personAttribute = b.personAttribute;
        this.proposedValue = b.proposedValue;
        this.resolutionStatus = (b.resolutionStatus != null ? b.resolutionStatus : ChangeItemResolutionStatus.PENDING);
        this.resolutionComment = b.resolutionComment;
    }

    // --- Getters ---
    public Long getId() {
        return id;
    }

    public ChangeRequest getChangeRequest() {
        return changeRequest;
    }

    public ChangeAction getAction() {
        return action;
    }

    public PersonAttribute getPersonAttribute() {
        return personAttribute;
    }

    public String getProposedValue() {
        return proposedValue;
    }

    public ChangeItemResolutionStatus getResolutionStatus() {
        return resolutionStatus;
    }

    public String getResolutionComment() {
        return resolutionComment;
    }

    // --- Setters ---
    public void setId(Long id) {
        this.id = id;
    }

    public void setChangeRequest(ChangeRequest changeRequest) {
        this.changeRequest = changeRequest;
    }

    public void setAction(ChangeAction action) {
        this.action = action;
    }

    public void setPersonAttribute(PersonAttribute personAttribute) {
        this.personAttribute = personAttribute;
    }

    public void setProposedValue(String proposedValue) {
        this.proposedValue = proposedValue;
    }

    public void setResolutionStatus(ChangeItemResolutionStatus resolutionStatus) {
        this.resolutionStatus = resolutionStatus;
    }

    public void setResolutionComment(String resolutionComment) {
        this.resolutionComment = resolutionComment;
    }

    // --- Builder ---
    public static class Builder {
        private Long id;
        private ChangeRequest changeRequest;
        private ChangeAction action;
        private PersonAttribute personAttribute;
        private String proposedValue;
        private ChangeItemResolutionStatus resolutionStatus;
        private String resolutionComment;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withChangeRequest(ChangeRequest changeRequest) {
            this.changeRequest = changeRequest;
            return this;
        }

        public Builder withAction(ChangeAction action) {
            this.action = action;
            return this;
        }

        public Builder withPersonAttribute(PersonAttribute personAttribute) {
            this.personAttribute = personAttribute;
            return this;
        }

        public Builder withProposedValue(String proposedValue) {
            this.proposedValue = proposedValue;
            return this;
        }

        public Builder withResolutionStatus(ChangeItemResolutionStatus resolutionStatus) {
            this.resolutionStatus = resolutionStatus;
            return this;
        }

        public Builder withResolutionComment(String resolutionComment) {
            this.resolutionComment = resolutionComment;
            return this;
        }

        public ChangeRequestItem build() {
            return new ChangeRequestItem(this);
        }
    }

    // --- equals / hashCode (id only) ---
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ChangeRequestItem))
            return false;
        ChangeRequestItem that = (ChangeRequestItem) o;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return "ChangeRequestItem{" +
                "id=" + id +
                ", crId=" + (changeRequest != null ? changeRequest.getId() : null) +
                ", action=" + action +
                ", personAttributeId=" + (personAttribute != null ? personAttribute.getId() : null) +
                ", proposedValue=" + proposedValue +
                ", resolutionStatus=" + resolutionStatus +
                ", resolutionComment=" + (resolutionComment != null ? "'" + resolutionComment + "'" : "null") +
                '}';
    }
}
