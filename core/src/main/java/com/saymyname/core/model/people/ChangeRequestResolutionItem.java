// src/main/java/com/saymyname/core/model/people/ChangeRequestResolutionItem.java
package com.saymyname.core.model.people;

import java.util.Objects;

import com.saymyname.core.model.enums.ChangeResolutionDecision;

/**
 * Décision unitaire sur un item d'une Change Request (commande).
 * - itemId : identifiant de l’item ciblé.
 * - decision : APPROVE / REJECT / CANCEL.
 * - resolutionComment : note optionnelle pour cet item (persistée côté item).
 */
public final class ChangeRequestResolutionItem {

    private final Long itemId;
    private final ChangeResolutionDecision decision;
    private final String resolutionComment; // optionnel

    private ChangeRequestResolutionItem(Builder b) {
        this.itemId = b.itemId;
        this.decision = b.decision;
        this.resolutionComment = b.resolutionComment;
    }

    public Long getItemId() {
        return itemId;
    }

    public ChangeResolutionDecision getDecision() {
        return decision;
    }

    public String getResolutionComment() {
        return resolutionComment;
    }

    /* ------------ Builder ------------ */
    public static final class Builder {
        private Long itemId;
        private ChangeResolutionDecision decision;
        private String resolutionComment;

        public Builder withItemId(Long itemId) {
            this.itemId = itemId;
            return this;
        }

        public Builder withDecision(ChangeResolutionDecision decision) {
            this.decision = decision;
            return this;
        }

        public Builder withResolutionComment(String resolutionComment) {
            this.resolutionComment = resolutionComment;
            return this;
        }

        public ChangeRequestResolutionItem build() {
            if (itemId == null)
                throw new IllegalStateException("itemId is required");
            if (decision == null)
                throw new IllegalStateException("decision is required");
            return new ChangeRequestResolutionItem(this);
        }
    }

    /* ------------ equals/hashCode/toString ------------ */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ChangeRequestResolutionItem))
            return false;
        ChangeRequestResolutionItem that = (ChangeRequestResolutionItem) o;
        return Objects.equals(itemId, that.itemId)
                && decision == that.decision
                && Objects.equals(resolutionComment, that.resolutionComment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, decision, resolutionComment);
    }

    @Override
    public String toString() {
        return "ChangeRequestResolutionItem{" +
                "itemId=" + itemId +
                ", decision=" + decision +
                ", resolutionComment=" + (resolutionComment != null ? "'" + resolutionComment + "'" : "null") +
                '}';
    }
}
