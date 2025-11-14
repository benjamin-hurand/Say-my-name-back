// src/main/java/com/saymyname/core/model/people/BulkChangeRequestResolution.java
package com.saymyname.core.model.people;

import com.saymyname.core.model.auth.User;
import java.util.List;
import java.util.Objects;

public final class BulkChangeRequestResolution {

    private final User resolver;
    private final List<Long> changeRequestIds;
    private final String decision; // "APPROVE" | "REJECT"
    private final String resolutionComment; // nullable

    private BulkChangeRequestResolution(Builder b) {
        this.resolver = b.resolver;
        this.changeRequestIds = List.copyOf(b.changeRequestIds);
        this.decision = b.decision;
        this.resolutionComment = b.resolutionComment;
    }

    public User getResolver() {
        return resolver;
    }

    public List<Long> getChangeRequestIds() {
        return changeRequestIds;
    }

    public String getDecision() {
        return decision;
    }

    public String getResolutionComment() {
        return resolutionComment;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private User resolver;
        private List<Long> changeRequestIds;
        private String decision;
        private String resolutionComment;

        public Builder resolver(User v) {
            this.resolver = v;
            return this;
        }

        public Builder changeRequestIds(List<Long> v) {
            this.changeRequestIds = v;
            return this;
        }

        public Builder decision(String v) {
            this.decision = v;
            return this;
        }

        public Builder resolutionComment(String v) {
            this.resolutionComment = v;
            return this;
        }

        public BulkChangeRequestResolution build() {
            Objects.requireNonNull(resolver, "resolver is required");
            Objects.requireNonNull(changeRequestIds, "changeRequestIds is required");
            Objects.requireNonNull(decision, "decision is required");
            return new BulkChangeRequestResolution(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof BulkChangeRequestResolution that))
            return false;
        return Objects.equals(resolver, that.resolver) &&
                Objects.equals(changeRequestIds, that.changeRequestIds) &&
                Objects.equals(decision, that.decision) &&
                Objects.equals(resolutionComment, that.resolutionComment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resolver, changeRequestIds, decision, resolutionComment);
    }

    @Override
    public String toString() {
        return "BulkChangeRequestResolution{resolver=%s,ids=%s,decision=%s,comment=%s}"
                .formatted(resolver != null ? resolver.getId() : null, changeRequestIds, decision, resolutionComment);
    }
}
