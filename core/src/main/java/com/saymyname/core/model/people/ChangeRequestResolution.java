// src/main/java/com/saymyname/core/model/people/ChangeRequestResolution.java
package com.saymyname.core.model.people;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.saymyname.core.model.auth.User;

/**
 * Commande métier immuable représentant la résolution d'une Change Request.
 * - changeRequestId : enveloppe ciblée
 * - resolver : admin (avec id)
 * - resolutionComment : note globale optionnelle
 * - decisions : liste d’items décidés (jamais null)
 */
public final class ChangeRequestResolution {

    private final Long changeRequestId;
    private final User resolver;
    private final String resolutionComment;
    private final List<ChangeRequestResolutionItem> decisions;

    private ChangeRequestResolution(Builder b) {
        this.changeRequestId = b.changeRequestId;
        this.resolver = b.resolver;
        this.resolutionComment = b.resolutionComment;
        this.decisions = Collections.unmodifiableList(new ArrayList<>(b.decisions));
    }

    public Long getChangeRequestId() {
        return changeRequestId;
    }

    public User getResolver() {
        return resolver;
    }

    public String getResolutionComment() {
        return resolutionComment;
    }

    public List<ChangeRequestResolutionItem> getDecisions() {
        return decisions;
    }

    public static final class Builder {
        private Long changeRequestId;
        private User resolver;
        private String resolutionComment;
        private List<ChangeRequestResolutionItem> decisions = new ArrayList<>();

        public Builder withChangeRequestId(Long id) {
            this.changeRequestId = id;
            return this;
        }

        public Builder withResolver(User resolver) {
            this.resolver = resolver;
            return this;
        }

        public Builder withResolutionComment(String c) {
            this.resolutionComment = c;
            return this;
        }

        public Builder withDecisions(List<ChangeRequestResolutionItem> items) {
            this.decisions = (items != null) ? new ArrayList<>(items) : new ArrayList<>();
            return this;
        }

        public Builder addDecision(ChangeRequestResolutionItem d) {
            if (d != null)
                this.decisions.add(d);
            return this;
        }

        public ChangeRequestResolution build() {
            if (changeRequestId == null)
                throw new IllegalStateException("changeRequestId is required");
            if (resolver == null || resolver.getId() == null)
                throw new IllegalStateException("resolver (with id) is required");
            if (decisions == null)
                decisions = new ArrayList<>();
            return new ChangeRequestResolution(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ChangeRequestResolution))
            return false;
        ChangeRequestResolution that = (ChangeRequestResolution) o;
        return Objects.equals(changeRequestId, that.changeRequestId)
                && Objects.equals(resolver, that.resolver)
                && Objects.equals(resolutionComment, that.resolutionComment)
                && Objects.equals(decisions, that.decisions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(changeRequestId, resolver, resolutionComment, decisions);
    }

    @Override
    public String toString() {
        Long resolverId = (resolver != null ? resolver.getId() : null);
        return "ChangeRequestResolution{" +
                "changeRequestId=" + changeRequestId +
                ", resolverId=" + resolverId +
                ", resolutionComment=" + (resolutionComment != null ? "'" + resolutionComment + "'" : "null") +
                ", decisions=" + decisions +
                '}';
    }
}
