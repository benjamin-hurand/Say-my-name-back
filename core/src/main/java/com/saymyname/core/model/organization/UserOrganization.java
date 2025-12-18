package com.saymyname.core.model.organization;

import java.time.LocalDateTime;
import java.util.Objects;

import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.model.people.Person;

public class UserOrganization {

    private Long userId;
    private Long organizationId;
    private OrgRole role;
    private LocalDateTime createdAt;

    /**
     * Id de la Person liée à cet utilisateur dans cette organisation (nullable).
     * Correspond à user_organizations.person_id.
     */
    private Long personId;

    /**
     * Agrégat optionnel si tu veux embarquer la Person complète.
     */
    private Person person;

    /**
     * Agrégat optionnel si tu veux embarquer l’Organisation complète.
     */
    private Organization organization;

    private UserOrganization(Builder builder) {
        this.userId = builder.userId;
        this.organizationId = builder.organizationId;
        this.role = builder.role;
        this.createdAt = builder.createdAt;
        this.personId = builder.personId;
        this.person = builder.person;
        this.organization = builder.organization;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public OrgRole getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getPersonId() {
        return personId;
    }

    public Person getPerson() {
        return person;
    }

    public Organization getOrganization() {
        return organization;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Long userId;
        private Long organizationId;
        private OrgRole role;
        private LocalDateTime createdAt;
        private Long personId;
        private Person person;
        private Organization organization;

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder organizationId(Long organizationId) {
            this.organizationId = organizationId;
            return this;
        }

        public Builder role(OrgRole role) {
            this.role = role;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder personId(Long personId) {
            this.personId = personId;
            return this;
        }

        public Builder person(Person person) {
            this.person = person;
            return this;
        }

        public Builder organization(Organization organization) {
            this.organization = organization;
            return this;
        }

        public UserOrganization build() {
            return new UserOrganization(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserOrganization that))
            return false;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(organizationId, that.organizationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, organizationId);
    }

    @Override
    public String toString() {
        return "UserOrganization{" +
                "userId=" + userId +
                ", organizationId=" + organizationId +
                ", role='" + role + '\'' +
                ", createdAt=" + createdAt +
                ", personId=" + personId +
                '}';
    }
}
