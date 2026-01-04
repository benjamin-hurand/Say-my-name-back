// src/main/java/com/saymyname/persistence/entity/UserEntity.java
package com.saymyname.persistence.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.saymyname.core.model.enums.AuthProvider;
import com.saymyname.core.model.enums.SrsAlgorithm;
import com.saymyname.persistence.jpa.UuidBytesConverter;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    @Convert(converter = UuidBytesConverter.class)
    @Column(name = "public_id", columnDefinition = "BINARY(16)", nullable = false, unique = true, updatable = false)
    private UUID publicId;

    @Column(name = "display_name", nullable = false, length = 50, unique = true)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "srs_algorithm", nullable = false, length = 16)
    private SrsAlgorithm srsAlgorithm = SrsAlgorithm.SM2;

    @Column(name = "roles", nullable = false, length = 255)
    private String roles;

    @Column(name = "active", nullable = false)
    private Boolean active;

    /** ✅ Global invalidation */
    @Column(name = "auth_version", nullable = false)
    private int authVersion = 0;

    /**
     * ✅ Audit.
     * DB gère DEFAULT CURRENT_TIMESTAMP + ON UPDATE CURRENT_TIMESTAMP.
     * On laisse la DB piloter, donc non-insérable/non-updatable côté JPA.
     */
    @Column(name = "auth_updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime authUpdatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("primary DESC, verifiedAt DESC, id ASC")
    private List<UserEmailEntity> emails = new ArrayList<>();

    /**
     * ✅ identities en Set : évite le "MultipleBagFetchException" quand on fetch
     * emails + identities.
     * L'ordre n'est pas un état métier -> Set est plus propre.
     *
     * Note: LinkedHashSet pour garder un ordre d'itération stable (insertion order)
     * côté Java,
     * sans le persister en base (contrairement à @OrderColumn).
     */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<UserIdentityEntity> identities = new LinkedHashSet<>();

    public UserEntity() {
    }

    public UserEntity(
            Long id,
            String displayName,
            SrsAlgorithm srsAlgorithm,
            String roles,
            Boolean active) {
        this.id = id;
        this.displayName = displayName;
        this.srsAlgorithm = (srsAlgorithm != null ? srsAlgorithm : SrsAlgorithm.SM2);
        this.roles = roles;
        this.active = active;
    }

    @PrePersist
    protected void onPrePersist() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID();
        }
    }

    // -------- Helpers emails --------
    public void addEmail(UserEmailEntity email) {
        if (email == null)
            return;
        this.emails.add(email);
        email.setUser(this);
    }

    public void removeEmail(UserEmailEntity email) {
        if (email == null)
            return;
        this.emails.remove(email);
        email.setUser(null);
    }

    // -------- Helpers identities --------
    public void addIdentity(UserIdentityEntity identity) {
        if (identity == null)
            return;
        this.identities.add(identity);
        identity.setUser(this);
    }

    public void removeIdentity(UserIdentityEntity identity) {
        if (identity == null)
            return;
        this.identities.remove(identity);
        identity.setUser(null);
    }

    // -------- Transients --------
    @Transient
    public String getPrimaryEmailValue() {
        if (emails == null)
            return null;
        for (UserEmailEntity e : emails) {
            if (e != null && e.isPrimary())
                return e.getEmail();
        }
        return null;
    }

    @Transient
    public boolean hasLocalPassword() {
        if (identities == null)
            return false;
        return identities.stream().anyMatch(i -> i != null
                && i.isEnabled()
                && i.getProvider() == AuthProvider.LOCAL
                && i.getPasswordHash() != null
                && !i.getPasswordHash().isBlank());
    }

    // -------- Getters / Setters --------
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getPublicId() {
        return publicId;
    }

    public void setPublicId(UUID publicId) {
        this.publicId = publicId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public SrsAlgorithm getSrsAlgorithm() {
        return srsAlgorithm;
    }

    public void setSrsAlgorithm(SrsAlgorithm srsAlgorithm) {
        this.srsAlgorithm = srsAlgorithm;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public int getAuthVersion() {
        return authVersion;
    }

    public void setAuthVersion(int authVersion) {
        this.authVersion = authVersion;
    }

    public LocalDateTime getAuthUpdatedAt() {
        return authUpdatedAt;
    }

    public List<UserEmailEntity> getEmails() {
        return emails;
    }

    public void setEmails(List<UserEmailEntity> emails) {
        this.emails.clear();
        if (emails != null) {
            for (UserEmailEntity e : emails)
                addEmail(e);
        }
    }

    public Set<UserIdentityEntity> getIdentities() {
        return identities;
    }

    public void setIdentities(Set<UserIdentityEntity> identities) {
        this.identities.clear();
        if (identities != null) {
            for (UserIdentityEntity i : identities)
                addIdentity(i);
        }
    }

    /**
     * Optionnel : pratique si tu reçois encore des List depuis des mappers
     * existants.
     */
    public void setIdentitiesFromList(List<UserIdentityEntity> identities) {
        this.identities.clear();
        if (identities != null) {
            for (UserIdentityEntity i : identities)
                addIdentity(i);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserEntity that))
            return false;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", publicId=" + (publicId != null ? publicId : "null") +
                ", displayName='" + displayName + '\'' +
                ", primaryEmail='" + getPrimaryEmailValue() + '\'' +
                ", srsAlgorithm=" + srsAlgorithm +
                ", roles='" + roles + '\'' +
                ", active=" + active +
                ", authVersion=" + authVersion +
                ", authUpdatedAt=" + authUpdatedAt +
                ", emailsCount=" + (emails != null ? emails.size() : 0) +
                ", identitiesCount=" + (identities != null ? identities.size() : 0) +
                '}';
    }
}
