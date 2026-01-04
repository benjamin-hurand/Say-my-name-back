// src/main/java/com/saymyname/persistence/entity/organization/UserOrganizationEntity.java
package com.saymyname.persistence.entity.organization;

import java.time.LocalDateTime;
import java.util.Objects;

import com.saymyname.core.model.enums.MembershipStatus;
import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.model.enums.PersonLinkStatus;
import com.saymyname.persistence.entity.UserEmailEntity;
import com.saymyname.persistence.entity.UserEntity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_organizations", uniqueConstraints = {
        @UniqueConstraint(name = "uq_uo_org_user", columnNames = { "organization_id", "user_id" }),
        @UniqueConstraint(name = "uq_uo_org_person", columnNames = { "organization_id", "person_id" })
}, indexes = {
        @Index(name = "ix_uo_org_display_name", columnList = "organization_id,display_name")
})
public class UserOrganizationEntity {

    @EmbeddedId
    private UserOrganizationId id = new UserOrganizationId();

    /** FK user_id (fait partie de la PK) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_uo_user"))
    private UserEntity user;

    /** FK organization_id (fait partie de la PK) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("organizationId")
    @JoinColumn(name = "organization_id", nullable = false, foreignKey = @ForeignKey(name = "fk_uo_org"))
    private OrganizationEntity organization;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    private OrgRole role;

    @Column(name = "display_name", length = 80)
    private String displayName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /** FK optionnelle vers persons.id */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", foreignKey = @ForeignKey(name = "fk_uo_person"))
    private PersonEntity person;

    /**
     * Statut du lien User ↔ Person dans l'organisation (indépendant du status
     * membership).
     * Correspond à user_organizations.person_link_status.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "person_link_status", nullable = false, length = 16)
    private PersonLinkStatus personLinkStatus = PersonLinkStatus.NONE;

    /**
     * Snapshot de la policy d'onboarding (issue de l'invitation au moment de
     * l'acceptation).
     */
    @Column(name = "can_pick_person", nullable = false)
    private boolean canPickPerson = false;

    @Column(name = "can_create_person", nullable = false)
    private boolean canCreatePerson = true;

    @Column(name = "pick_requires_approval", nullable = false)
    private boolean pickRequiresApproval = true;

    @Column(name = "create_requires_approval", nullable = false)
    private boolean createRequiresApproval = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private MembershipStatus status = MembershipStatus.ACTIVE;

    /** FK optionnelle vers user_emails.id */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_email_id", foreignKey = @ForeignKey(name = "fk_uo_pref_email"))
    private UserEmailEntity preferredEmail;

    public UserOrganizationEntity() {
        // JPA
    }

    public UserOrganizationEntity(UserEntity user, OrganizationEntity organization, OrgRole role) {
        setUser(user);
        setOrganization(organization);
        this.role = role;
    }

    @PrePersist
    protected void onPrePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (personLinkStatus == null) {
            personLinkStatus = PersonLinkStatus.NONE;
        }

        // sécurité : si jamais setUser/setOrganization n'ont pas été appelés
        if (id == null)
            id = new UserOrganizationId();
        if (user != null)
            id.setUserId(user.getId());
        if (organization != null)
            id.setOrganizationId(organization.getId());
    }

    // ----- Getters / Setters -----

    public UserOrganizationId getId() {
        return id;
    }

    public void setId(UserOrganizationId id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
        if (user != null) {
            if (this.id == null)
                this.id = new UserOrganizationId();
            this.id.setUserId(user.getId());
        }
    }

    public OrganizationEntity getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationEntity organization) {
        this.organization = organization;
        if (organization != null) {
            if (this.id == null)
                this.id = new UserOrganizationId();
            this.id.setOrganizationId(organization.getId());
        }
    }

    public OrgRole getRole() {
        return role;
    }

    public void setRole(OrgRole role) {
        this.role = role;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public PersonEntity getPerson() {
        return person;
    }

    public void setPerson(PersonEntity person) {
        this.person = person;
    }

    public PersonLinkStatus getPersonLinkStatus() {
        return personLinkStatus;
    }

    public void setPersonLinkStatus(PersonLinkStatus personLinkStatus) {
        this.personLinkStatus = (personLinkStatus != null ? personLinkStatus : PersonLinkStatus.NONE);
    }

    public boolean isCanPickPerson() {
        return canPickPerson;
    }

    public void setCanPickPerson(boolean canPickPerson) {
        this.canPickPerson = canPickPerson;
    }

    public boolean isCanCreatePerson() {
        return canCreatePerson;
    }

    public void setCanCreatePerson(boolean canCreatePerson) {
        this.canCreatePerson = canCreatePerson;
    }

    public boolean isPickRequiresApproval() {
        return pickRequiresApproval;
    }

    public void setPickRequiresApproval(boolean pickRequiresApproval) {
        this.pickRequiresApproval = pickRequiresApproval;
    }

    public boolean isCreateRequiresApproval() {
        return createRequiresApproval;
    }

    public void setCreateRequiresApproval(boolean createRequiresApproval) {
        this.createRequiresApproval = createRequiresApproval;
    }

    public MembershipStatus getStatus() {
        return status;
    }

    public void setStatus(MembershipStatus status) {
        this.status = (status != null ? status : MembershipStatus.ACTIVE);
    }

    public UserEmailEntity getPreferredEmail() {
        return preferredEmail;
    }

    public void setPreferredEmail(UserEmailEntity preferredEmail) {
        this.preferredEmail = preferredEmail;
    }

    // ----- equals/hashCode sur la PK composite -----

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserOrganizationEntity that))
            return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "UserOrganizationEntity{" +
                "id=" + id +
                ", role=" + role +
                ", status=" + status +
                ", personId=" + (person != null ? person.getId() : null) +
                ", personLinkStatus=" + personLinkStatus +
                ", canPickPerson=" + canPickPerson +
                ", canCreatePerson=" + canCreatePerson +
                ", pickRequiresApproval=" + pickRequiresApproval +
                ", createRequiresApproval=" + createRequiresApproval +
                '}';
    }
}
