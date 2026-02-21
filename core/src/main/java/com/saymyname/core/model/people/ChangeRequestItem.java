// src/main/java/com/saymyname/core/model/people/ChangeRequestItem.java
package com.saymyname.core.model.people;

import com.saymyname.core.model.enums.ChangeAction;
import com.saymyname.core.model.enums.ChangeRequestItemStatus;

import java.util.Objects;

/**
 * Ligne métier d’une demande (CREATE/UPDATE/DELETE).
 * - CREATE : fact = null, proposedValue requis
 * - UPDATE : fact requis, proposedValue requis
 * - DELETE : fact requis, proposedValue null
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
    private Fact fact;

    /** Valeur proposée (null pour DELETE) */
    private String proposedValue;

    /** Statut de résolution (par item) — PENDING par défaut */
    private ChangeRequestItemStatus resolutionStatus = ChangeRequestItemStatus.PENDING;

    /** Commentaire de résolution (facultatif, utile surtout pour REJECTED) */
    private String resolutionComment;

    public ChangeRequestItem() {
    }

    private ChangeRequestItem(Builder b) {
        this.id = b.id;
        this.changeRequest = b.changeRequest;
        this.action = b.action;
        this.fact = b.fact;
        this.proposedValue = b.proposedValue;
        this.resolutionStatus = (b.resolutionStatus != null ? b.resolutionStatus : ChangeRequestItemStatus.PENDING);
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

    public Fact getFact() {
        return fact;
    }

    public String getProposedValue() {
        return proposedValue;
    }

    public ChangeRequestItemStatus getResolutionStatus() {
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

    public void setFact(Fact fact) {
        this.fact = fact;
    }

    public void setProposedValue(String proposedValue) {
        this.proposedValue = proposedValue;
    }

    public void setResolutionStatus(ChangeRequestItemStatus resolutionStatus) {
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
        private Fact fact;
        private String proposedValue;
        private ChangeRequestItemStatus resolutionStatus;
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

        public Builder withFact(Fact fact) {
            this.fact = fact;
            return this;
        }

        public Builder withProposedValue(String proposedValue) {
            this.proposedValue = proposedValue;
            return this;
        }

        public Builder withResolutionStatus(ChangeRequestItemStatus resolutionStatus) {
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
                ", factId=" + (fact != null ? fact.getId() : null) +
                ", proposedValue=" + proposedValue +
                ", resolutionStatus=" + resolutionStatus +
                ", resolutionComment=" + (resolutionComment != null ? "'" + resolutionComment + "'" : "null") +
                '}';
    }
}
