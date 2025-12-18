package com.saymyname.persistence.entity.organization.invitation;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.saymyname.core.model.enums.InvitationType;
import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.PersonEntity;
import com.saymyname.persistence.multitenancy.BaseOrgScoped;

@Entity
@Table(name = "invitations", indexes = {
        @Index(name = "ix_invit_org_type", columnList = "organization_id,type"),
        @Index(name = "ix_invit_org_email", columnList = "organization_id,email"),
        @Index(name = "ix_invit_org_expires", columnList = "organization_id,expires_at,revoked_at"),
        @Index(name = "ix_invit_org_person", columnList = "organization_id,person_id"),
        @Index(name = "uq_invit_token", columnList = "token_hash", unique = true),
        @Index(name = "uq_invit_id_org", columnList = "id,organization_id", unique = true)
})
public class InvitationEntity extends BaseOrgScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // EMAIL / SELF_SERVICE
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 16)
    private InvitationType type;

    @Column(name = "label", length = 80)
    private String label;

    @Column(name = "note", length = 255)
    private String note;

    // JSON (MySQL 8) — stocké en String pour rester portable
    @Column(name = "constraints_json", columnDefinition = "json")
    private String constraintsJson;

    // Rôle cible dans l'orga
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 64)
    private OrgRole role;

    // Email cible si type = EMAIL
    @Column(name = "email", length = 320)
    private String email;

    // === FK SCALAIRE écrite par JPA
    @Column(name = "person_id")
    private Long personId;

    // Association READ-ONLY (évite le mix insertable/updatable avec
    // BaseOrgScoped.organization_id)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "person_id", referencedColumnName = "id", insertable = false, updatable = false),
            @JoinColumn(name = "organization_id", referencedColumnName = "organization_id", insertable = false, updatable = false)
    })
    private PersonEntity person;

    // Hash du token (SHA-256/HMAC) → 32 octets
    @Column(name = "token_hash", nullable = false, columnDefinition = "varbinary(32)")
    private byte[] tokenHash;

    // Hash du PIN au format PHC (ex: $argon2id$v=19$...): chaîne
    @Column(name = "pin_hash_phc", length = 255)
    private String pinHashPhc;

    // null = illimité
    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "uses_count", nullable = false)
    private int usesCount;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    // Auteur
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private UserEntity createdBy;

    // Default DB CURRENT_TIMESTAMP
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    // Utilisateur qui a accepté
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepted_by")
    private UserEntity acceptedBy;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    // Journal des usages (append-only) — ON DELETE CASCADE en DB
    @OneToMany(mappedBy = "invitation", fetch = FetchType.LAZY)
    @OrderBy("usedAt DESC")
    private List<InvitationUsageEntity> usages = new ArrayList<>();

    // --- Getters / Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public InvitationType getType() {
        return type;
    }

    public void setType(InvitationType type) {
        this.type = type;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getConstraintsJson() {
        return constraintsJson;
    }

    public void setConstraintsJson(String constraintsJson) {
        this.constraintsJson = constraintsJson;
    }

    public OrgRole getRole() {
        return role;
    }

    public void setRole(OrgRole role) {
        this.role = role;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    /**
     * Synchronise l'association et la FK scalaire.
     * (organization_id doit déjà être positionné dans BaseOrgScoped.)
     */
    public void setPerson(PersonEntity person) {
        this.person = person;
        this.personId = (person != null ? person.getId() : null);
    }

    public byte[] getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(byte[] tokenHash) {
        this.tokenHash = tokenHash;
    }

    public String getPinHashPhc() {
        return pinHashPhc;
    }

    public void setPinHashPhc(String pinHashPhc) {
        this.pinHashPhc = pinHashPhc;
    }

    public Integer getMaxUses() {
        return maxUses;
    }

    public void setMaxUses(Integer maxUses) {
        this.maxUses = maxUses;
    }

    public int getUsesCount() {
        return usesCount;
    }

    public void setUsesCount(int usesCount) {
        this.usesCount = usesCount;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    public UserEntity getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UserEntity createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public UserEntity getAcceptedBy() {
        return acceptedBy;
    }

    public void setAcceptedBy(UserEntity acceptedBy) {
        this.acceptedBy = acceptedBy;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public List<InvitationUsageEntity> getUsages() {
        return usages;
    }

    public void setUsages(List<InvitationUsageEntity> usages) {
        this.usages = (usages != null ? usages : new ArrayList<>());
    }

    // --- equals / hashCode (id only)

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof InvitationEntity))
            return false;
        InvitationEntity that = (InvitationEntity) o;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return "InvitationEntity{" +
                "id=" + id +
                ", type=" + type +
                ", role='" + role + '\'' +
                ", email='" + email + '\'' +
                ", maxUses=" + maxUses +
                ", usesCount=" + usesCount +
                ", expiresAt=" + expiresAt +
                ", revokedAt=" + revokedAt +
                ", createdBy=" + (createdBy != null ? createdBy.getId() : null) +
                ", acceptedBy=" + (acceptedBy != null ? acceptedBy.getId() : null) +
                '}';
    }
}
