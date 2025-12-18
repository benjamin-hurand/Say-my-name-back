package com.saymyname.persistence.entity.organization;

import com.saymyname.core.model.enums.OrgRole;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "user_organizations")
public class UserOrganizationEntity {

    @EmbeddedId
    private UserOrganizationId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("organizationId") // mappe la FK organization_id dans l'EmbeddedId
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationEntity organization;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    private OrgRole role;

    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "timestamp default current_timestamp")
    private LocalDateTime createdAt;

    /**
     * FK brute vers persons.id (nullable).
     * Correspond à la colonne user_organizations.person_id.
     */
    @Column(name = "person_id")
    private Long personId;

    /**
     * Relation vers PersonEntity, basée sur person_id.
     * Marquée insertable/updatable=false pour ne pas entrer en conflit
     * avec le champ personId.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", insertable = false, updatable = false)
    private PersonEntity person;

    public UserOrganizationEntity() {
    }

    public UserOrganizationEntity(UserOrganizationId id,
            OrganizationEntity organization,
            OrgRole role,
            LocalDateTime createdAt,
            Long personId) {
        this.id = id;
        this.organization = organization;
        this.role = role;
        this.createdAt = createdAt;
        this.personId = personId;
    }

    public UserOrganizationId getId() {
        return id;
    }

    public void setId(UserOrganizationId id) {
        this.id = id;
    }

    public OrganizationEntity getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationEntity organization) {
        this.organization = organization;
    }

    public OrgRole getRole() {
        return role;
    }

    public void setRole(OrgRole role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public PersonEntity getPerson() {
        return person;
    }

    public void setPerson(PersonEntity person) {
        this.person = person;
        // optionnel : si tu veux garder personId cohérent quand tu sets la relation :
        // this.personId = (person != null ? person.getId() : null);
    }

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
                ", createdAt=" + createdAt +
                ", personId=" + personId +
                '}';
    }
}
