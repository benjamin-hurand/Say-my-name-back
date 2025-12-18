// src/main/java/com/saymyname/persistence/entity/organization/people/PersonEmailEntity.java
package com.saymyname.persistence.entity.organization.people;

import java.time.LocalDateTime;
import java.util.Objects;

import com.saymyname.core.model.enums.EmailKind;
import com.saymyname.core.model.enums.EmailSourceKind;
import com.saymyname.persistence.entity.organization.PersonEntity;
import com.saymyname.persistence.multitenancy.BaseOrgScoped;

import jakarta.persistence.*;

@Entity
@Table(name = "person_emails", indexes = {
        @Index(name = "idx_person_emails_org", columnList = "organization_id"),
        @Index(name = "idx_pe_person_org", columnList = "person_id,organization_id"),
        @Index(name = "idx_person_emails_id_org", columnList = "id,organization_id"),
        @Index(name = "idx_pe_org_email", columnList = "organization_id,email"),
        @Index(name = "idx_pe_primary", columnList = "person_id,organization_id,is_primary"),
        @Index(name = "uq_pe_person_email", columnList = "person_id,organization_id,email", unique = true)
})
public class PersonEmailEntity extends BaseOrgScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Pilote la FK côté JPA (écriture). */
    @Column(name = "person_id", nullable = false)
    private Long personId;

    /**
     * Relation read-only pour éviter le mix insertable/updatable dans la même
     * propriété.
     * On garde le composite join pour refléter le FK DB, mais en lecture seule.
     * L’écriture se fait via personId et organizationId (hérité de BaseOrgScoped).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "person_id", referencedColumnName = "id", insertable = false, updatable = false),
            @JoinColumn(name = "organization_id", referencedColumnName = "organization_id", insertable = false, updatable = false)
    })
    private PersonEntity person;

    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 16)
    private EmailKind kind = EmailKind.WORK;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_kind", nullable = false, length = 16)
    private EmailSourceKind sourceKind = EmailSourceKind.IMPORT;

    @Column(name = "source_label", length = 255)
    private String sourceLabel;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "bounced_at")
    private LocalDateTime bouncedAt;

    // timestamps générés en DB (triggers / default), donc read-only côté JPA
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime updatedAt;

    // --- Getters / Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /** ID technique de la personne (écriture FK) */
    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    /**
     * Navigation read-only. Utiliser setPersonId(+ setOrganizationId) pour écrire.
     */
    public PersonEntity getPerson() {
        return person;
    }

    /**
     * Autorisé pour confort : positionne aussi personId, et si orgId absent,
     * calque l’organizationId depuis la Person.
     */
    public void setPerson(PersonEntity person) {
        this.person = person;
        this.personId = (person != null ? person.getId() : null);
        if (person != null && this.getOrganizationId() == null) {
            // BaseOrgScoped expose normalement get/setOrganizationId()
            this.setOrganizationId(person.getOrganizationId());
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public EmailKind getKind() {
        return kind;
    }

    public void setKind(EmailKind kind) {
        this.kind = kind;
    }

    public EmailSourceKind getSourceKind() {
        return sourceKind;
    }

    public void setSourceKind(EmailSourceKind sourceKind) {
        this.sourceKind = sourceKind;
    }

    public String getSourceLabel() {
        return sourceLabel;
    }

    public void setSourceLabel(String sourceLabel) {
        this.sourceLabel = sourceLabel;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public LocalDateTime getBouncedAt() {
        return bouncedAt;
    }

    public void setBouncedAt(LocalDateTime bouncedAt) {
        this.bouncedAt = bouncedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // --- equals/hashCode sur id uniquement
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PersonEmailEntity))
            return false;
        PersonEmailEntity that = (PersonEmailEntity) o;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return "PersonEmailEntity{" +
                "id=" + id +
                ", personId=" + personId +
                ", email='" + email + '\'' +
                ", kind=" + kind +
                ", sourceKind=" + sourceKind +
                ", primary=" + primary +
                ", active=" + active +
                '}';
    }
}
