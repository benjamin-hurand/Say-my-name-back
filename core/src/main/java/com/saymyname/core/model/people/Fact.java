package com.saymyname.core.model.people;

import com.saymyname.core.model.enums.tenant.ScopeKind;

import java.time.LocalDateTime;
import java.util.Objects;

public class Fact {

    private Long id;

    // scope
    private ScopeKind scopeKind;
    private Long workspaceId;
    private Long teamId;

    private Person person;
    private Attribute attribute;

    // ids (hot path friendly)
    private Long personId;
    private Long attributeId;

    // value
    private String value;

    // validity
    private LocalDateTime validFrom;
    private LocalDateTime validTo;

    private boolean deleted;

    public Fact() {
    }

    private Fact(Builder b) {
        this.id = b.id;
        this.scopeKind = b.scopeKind;
        this.workspaceId = b.workspaceId;
        this.teamId = b.teamId;
        this.person = b.person;
        this.attribute = b.attribute;
        this.personId = b.personId;
        this.attributeId = b.attributeId;
        this.value = b.value;
        this.validFrom = b.validFrom;
        this.validTo = b.validTo;
        this.deleted = b.deleted;
    }

    // -------- getters/setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ScopeKind getScopeKind() {
        return scopeKind;
    }

    public void setScopeKind(ScopeKind scopeKind) {
        this.scopeKind = scopeKind;
    }

    public Long getWorkspaceId() {
        return workspaceId;
    }

    public void setWorkspaceId(Long workspaceId) {
        this.workspaceId = workspaceId;
    }

    public Long getTeamId() {
        return teamId;
    }

    public void setTeamId(Long teamId) {
        this.teamId = teamId;
    }

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    public Long getPersonId() {
        if (personId != null)
            return personId;
        return person != null ? person.getId() : null;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public Long getAttributeId() {
        if (attributeId != null)
            return attributeId;
        return attribute != null ? attribute.getId() : null;
    }

    public void setAttributeId(Long attributeId) {
        this.attributeId = attributeId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDateTime getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDateTime validTo) {
        this.validTo = validTo;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    // -------- Builder

    public static class Builder {
        private Long id;

        private ScopeKind scopeKind;
        private Long workspaceId;
        private Long teamId;

        private Person person;
        private Attribute attribute;

        private Long personId;
        private Long attributeId;

        private String value;
        private LocalDateTime validFrom;
        private LocalDateTime validTo;

        private boolean deleted;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withScopeKind(ScopeKind scopeKind) {
            this.scopeKind = scopeKind;
            return this;
        }

        public Builder withWorkspaceId(Long workspaceId) {
            this.workspaceId = workspaceId;
            return this;
        }

        public Builder withTeamId(Long teamId) {
            this.teamId = teamId;
            return this;
        }

        public Builder withPerson(Person person) {
            this.person = person;
            return this;
        }

        public Builder withAttribute(Attribute attribute) {
            this.attribute = attribute;
            return this;
        }

        public Builder withPersonId(Long personId) {
            this.personId = personId;
            return this;
        }

        public Builder withAttributeId(Long attributeId) {
            this.attributeId = attributeId;
            return this;
        }

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public Builder withValidFrom(LocalDateTime validFrom) {
            this.validFrom = validFrom;
            return this;
        }

        public Builder withValidTo(LocalDateTime validTo) {
            this.validTo = validTo;
            return this;
        }

        public Builder withDeleted(boolean deleted) {
            this.deleted = deleted;
            return this;
        }

        public Fact build() {
            return new Fact(this);
        }
    }

    /**
     * Id-only equality to avoid subtle bugs and heavy comparisons.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Fact))
            return false;
        Fact that = (Fact) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : 0;
    }

    @Override
    public String toString() {
        return "Fact{" +
                "id=" + id +
                ", scopeKind=" + scopeKind +
                ", workspaceId=" + workspaceId +
                ", teamId=" + teamId +
                ", personId=" + getPersonId() +
                ", attributeId=" + getAttributeId() +
                ", value='" + value + '\'' +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                ", deleted=" + deleted +
                '}';
    }
}