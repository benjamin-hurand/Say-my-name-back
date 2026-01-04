package com.saymyname.core.model.organization;

import java.time.LocalDateTime;
import java.util.Objects;

import com.saymyname.core.model.enums.MembershipStatus;
import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.model.enums.PersonLinkStatus;
import com.saymyname.core.model.people.Person;

public class UserOrganization {

    private Long userId;
    private Long organizationId;
    private OrgRole role;
    private LocalDateTime createdAt;

    /**
     * Statut membership (accès au groupe).
     * Correspond à user_organizations.status.
     */
    private MembershipStatus status;

    /**
     * Id de la Person liée à cet utilisateur dans cette organisation (nullable).
     * Correspond à user_organizations.person_id.
     */
    private Long personId;

    /**
     * Statut du lien identité (indépendant de status membership).
     * Correspond à user_organizations.person_link_status.
     */
    private PersonLinkStatus personLinkStatus;

    /**
     * Snapshot de la policy d’onboarding (dérivée de l’invitation au moment de
     * l’accept).
     */
    private boolean canPickPerson;
    private boolean canCreatePerson;
    private boolean pickRequiresApproval;
    private boolean createRequiresApproval;

    /**
     * Agrégats optionnels.
     */
    private Person person;
    private Organization organization;

    private UserOrganization(Builder builder) {
        this.userId = builder.userId;
        this.organizationId = builder.organizationId;
        this.role = builder.role;
        this.createdAt = builder.createdAt;

        this.status = builder.status;

        this.personId = builder.personId;
        this.personLinkStatus = builder.personLinkStatus;

        this.canPickPerson = builder.canPickPerson;
        this.canCreatePerson = builder.canCreatePerson;
        this.pickRequiresApproval = builder.pickRequiresApproval;
        this.createRequiresApproval = builder.createRequiresApproval;

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

    public MembershipStatus getStatus() {
        return status;
    }

    public Long getPersonId() {
        return personId;
    }

    public PersonLinkStatus getPersonLinkStatus() {
        return personLinkStatus;
    }

    public boolean isCanPickPerson() {
        return canPickPerson;
    }

    public boolean isCanCreatePerson() {
        return canCreatePerson;
    }

    public boolean isPickRequiresApproval() {
        return pickRequiresApproval;
    }

    public boolean isCreateRequiresApproval() {
        return createRequiresApproval;
    }

    public Person getPerson() {
        return person;
    }

    public Organization getOrganization() {
        return organization;
    }

    /**
     * Capacité effective : pick possible seulement s’il y a des persons pickables.
     * (à calculer plutôt côté service/dto avec un count).
     */
    public boolean canPickEffective(long pickableCount) {
        return canPickPerson && pickableCount > 0;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long userId;
        private Long organizationId;
        private OrgRole role;
        private LocalDateTime createdAt;

        private MembershipStatus status = MembershipStatus.ACTIVE;

        private Long personId;
        private PersonLinkStatus personLinkStatus = PersonLinkStatus.NONE;

        private boolean canPickPerson = false;
        private boolean canCreatePerson = true;
        private boolean pickRequiresApproval = true;
        private boolean createRequiresApproval = false;

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

        public Builder status(MembershipStatus status) {
            this.status = (status != null ? status : MembershipStatus.ACTIVE);
            return this;
        }

        public Builder personId(Long personId) {
            this.personId = personId;
            return this;
        }

        public Builder personLinkStatus(PersonLinkStatus personLinkStatus) {
            this.personLinkStatus = personLinkStatus == null ? PersonLinkStatus.NONE : personLinkStatus;
            return this;
        }

        public Builder canPickPerson(boolean canPickPerson) {
            this.canPickPerson = canPickPerson;
            return this;
        }

        public Builder canCreatePerson(boolean canCreatePerson) {
            this.canCreatePerson = canCreatePerson;
            return this;
        }

        public Builder pickRequiresApproval(boolean pickRequiresApproval) {
            this.pickRequiresApproval = pickRequiresApproval;
            return this;
        }

        public Builder createRequiresApproval(boolean createRequiresApproval) {
            this.createRequiresApproval = createRequiresApproval;
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
                ", role=" + role +
                ", createdAt=" + createdAt +
                ", status=" + status +
                ", personId=" + personId +
                ", personLinkStatus=" + personLinkStatus +
                ", canPickPerson=" + canPickPerson +
                ", canCreatePerson=" + canCreatePerson +
                ", pickRequiresApproval=" + pickRequiresApproval +
                ", createRequiresApproval=" + createRequiresApproval +
                '}';
    }
}
