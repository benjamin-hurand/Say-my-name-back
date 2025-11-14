// src/main/java/com/saymyname/core/model/people/queries/ChangeRequestListQuery.java
package com.saymyname.core.model.people;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;

import com.saymyname.core.model.enums.ChangeRequestStatus;

public class ChangeRequestListQuery {

    private Integer page;
    private Integer size;
    private List<ChangeRequestStatus> statuses;
    private Long personId;
    private Long submittedByUserId;
    private Long attributeId;
    private String action;
    private String sort;
    private String q;
    private OffsetDateTime from;
    private OffsetDateTime to;

    private ChangeRequestListQuery(Builder b) {
        this.page = b.page;
        this.size = b.size;
        this.statuses = b.statuses;
        this.personId = b.personId;
        this.submittedByUserId = b.submittedByUserId;
        this.attributeId = b.attributeId;
        this.action = b.action;
        this.sort = b.sort;
        this.q = b.q;
        this.from = b.from;
        this.to = b.to;
    }

    public Integer page() {
        return page;
    }

    public Integer size() {
        return size;
    }

    public List<ChangeRequestStatus> statuses() {
        return statuses;
    }

    public Long personId() {
        return personId;
    }

    public Long submittedByUserId() {
        return submittedByUserId;
    }

    public Long attributeId() {
        return attributeId;
    }

    public String action() {
        return action;
    }

    public String sort() {
        return sort;
    }

    public String q() {
        return q;
    }

    public OffsetDateTime from() {
        return from;
    }

    public OffsetDateTime to() {
        return to;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer page;
        private Integer size;
        private List<ChangeRequestStatus> statuses;
        private Long personId;
        private Long submittedByUserId;
        private Long attributeId;
        private String action;
        private String sort;
        private String q;
        private OffsetDateTime from;
        private OffsetDateTime to;

        public Builder page(Integer v) {
            this.page = v;
            return this;
        }

        public Builder size(Integer v) {
            this.size = v;
            return this;
        }

        public Builder statuses(List<ChangeRequestStatus> v) {
            this.statuses = v;
            return this;
        }

        public Builder personId(Long v) {
            this.personId = v;
            return this;
        }

        public Builder submittedByUserId(Long v) {
            this.submittedByUserId = v;
            return this;
        }

        public Builder attributeId(Long v) {
            this.attributeId = v;
            return this;
        }

        public Builder action(String v) {
            this.action = v;
            return this;
        }

        public Builder sort(String v) {
            this.sort = v;
            return this;
        }

        public Builder q(String v) {
            this.q = v;
            return this;
        }

        public Builder from(OffsetDateTime v) {
            this.from = v;
            return this;
        }

        public Builder to(OffsetDateTime v) {
            this.to = v;
            return this;
        }

        public ChangeRequestListQuery build() {
            return new ChangeRequestListQuery(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ChangeRequestListQuery that))
            return false;
        return Objects.equals(page, that.page) &&
                Objects.equals(size, that.size) &&
                Objects.equals(statuses, that.statuses) &&
                Objects.equals(personId, that.personId) &&
                Objects.equals(submittedByUserId, that.submittedByUserId) &&
                Objects.equals(attributeId, that.attributeId) &&
                Objects.equals(action, that.action) &&
                Objects.equals(sort, that.sort) &&
                Objects.equals(q, that.q) &&
                Objects.equals(from, that.from) &&
                Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(page, size, statuses, personId, submittedByUserId, attributeId, action, sort, q, from, to);
    }

    @Override
    public String toString() {
        return "ChangeRequestListQuery{page=%s,size=%s,status=%s,personId=%s,submittedByUserId=%s,attributeId=%s,action=%s,sort=%s,q=%s,from=%s,to=%s}"
                .formatted(page, size, statuses, personId, submittedByUserId, attributeId, action, sort, q, from, to);
    }
}
