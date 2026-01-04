// src/main/java/com/saymyname/core/model/auth/User.java
package com.saymyname.core.model.auth;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.saymyname.core.model.enums.AuthProvider;
import com.saymyname.core.model.enums.SrsAlgorithm;

public class User {

    private Long id;
    /** Identifiant public stable (UUID), exposable côté front. */
    private UUID publicId;

    private String displayName;
    private SrsAlgorithm srsAlgorithm = SrsAlgorithm.SM2;

    /** Chaîne CSV de rôles, ex: "ROLE_USER,ROLE_ADMIN". */
    private String roles;
    private Boolean active;

    /** ✅ Invalidation globale sessions (JWT + refresh). */
    private int authVersion;

    /** ✅ Audit (aligné DB). */
    private LocalDateTime authUpdatedAt;

    /** Emails liés au compte (jamais null ; liste vide par défaut). */
    private List<UserEmail> emails = Collections.emptyList();

    /**
     * ✅ Identities en Set : pas de doublons + reflète mieux le domaine.
     * (Le back peut les exposer ensuite en List si tu veux une API stable côté
     * front.)
     */
    private Set<UserIdentity> identities = Collections.emptySet();

    public User() {
    }

    private User(Builder b) {
        this.id = b.id;
        this.publicId = b.publicId;
        this.displayName = b.displayName;
        this.srsAlgorithm = b.srsAlgorithm != null ? b.srsAlgorithm : SrsAlgorithm.SM2;
        this.roles = b.roles;
        this.active = b.active;

        this.authVersion = b.authVersion;
        this.authUpdatedAt = b.authUpdatedAt;

        setEmails(b.emails);
        setIdentities(b.identities);
    }

    // ---------- Getters ----------
    public Long getId() {
        return id;
    }

    public UUID getPublicId() {
        return publicId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public SrsAlgorithm getSrsAlgorithm() {
        return srsAlgorithm;
    }

    public String getRoles() {
        return roles;
    }

    public Boolean isActive() {
        return active;
    }

    public int getAuthVersion() {
        return authVersion;
    }

    public LocalDateTime getAuthUpdatedAt() {
        return authUpdatedAt;
    }

    public List<UserEmail> getEmails() {
        return emails;
    }

    public Set<UserIdentity> getIdentities() {
        return identities;
    }

    /**
     * Optionnel: si tu veux garder une API "List" pour certains usages existants.
     */
    public List<UserIdentity> getIdentitiesList() {
        if (identities == null || identities.isEmpty())
            return List.of();
        return new ArrayList<>(identities);
    }

    /** Rétro-compat : renvoie la valeur de l'email primaire, ou null si absent. */
    public String getEmail() {
        return getPrimaryEmailValue();
    }

    public String getPrimaryEmailValue() {
        if (emails == null)
            return null;
        for (UserEmail e : emails) {
            if (Boolean.TRUE.equals(e.isPrimary()))
                return e.getEmail();
        }
        return null;
    }

    public Optional<UserEmail> getPrimaryEmail() {
        if (emails == null)
            return Optional.empty();
        return emails.stream().filter(e -> Boolean.TRUE.equals(e.isPrimary())).findFirst();
    }

    // ---------- Identities helpers ----------
    public boolean hasAuthProvider(AuthProvider provider) {
        if (provider == null || identities == null)
            return false;
        return identities.stream().anyMatch(i -> i != null && i.isEnabled() && provider.equals(i.getProvider()));
    }

    public boolean hasLocalPassword() {
        if (identities == null)
            return false;
        return identities.stream().anyMatch(i -> i != null
                && i.isEnabled()
                && i.getProvider() == AuthProvider.LOCAL
                && i.getPasswordHash() != null
                && !i.getPasswordHash().isBlank());
    }

    public Optional<UserIdentity> getLocalIdentity() {
        if (identities == null)
            return Optional.empty();
        return identities.stream()
                .filter(i -> i != null && i.isEnabled() && i.getProvider() == AuthProvider.LOCAL)
                .findFirst();
    }

    // ---------- Setters ----------
    public void setId(Long id) {
        this.id = id;
    }

    public void setPublicId(UUID publicId) {
        this.publicId = publicId;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setSrsAlgorithm(SrsAlgorithm srsAlgorithm) {
        this.srsAlgorithm = srsAlgorithm;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setAuthVersion(int authVersion) {
        this.authVersion = authVersion;
    }

    public void setAuthUpdatedAt(LocalDateTime authUpdatedAt) {
        this.authUpdatedAt = authUpdatedAt;
    }

    /** Copie défensive et jamais null. */
    public void setEmails(List<UserEmail> emails) {
        this.emails = (emails != null) ? new ArrayList<>(emails) : Collections.emptyList();
    }

    public void addEmail(UserEmail email) {
        if (email == null)
            return;
        if (this.emails == null || this.emails == Collections.<UserEmail>emptyList()) {
            this.emails = new ArrayList<>();
        }
        this.emails.add(email);
    }

    public void removeEmail(UserEmail email) {
        if (email == null || this.emails == null || this.emails.isEmpty())
            return;
        this.emails.remove(email);
    }

    /** Copie défensive et jamais null. */
    public void setIdentities(Set<UserIdentity> identities) {
        this.identities = (identities != null) ? new LinkedHashSet<>(identities) : Collections.emptySet();
    }

    /** Optionnel : permet de garder des mappers existants qui sortent des List. */
    public void setIdentitiesFromList(List<UserIdentity> identities) {
        if (identities == null) {
            this.identities = Collections.emptySet();
        } else {
            this.identities = new LinkedHashSet<>(identities);
        }
    }

    public void addIdentity(UserIdentity identity) {
        if (identity == null)
            return;
        if (this.identities == null || this.identities == Collections.<UserIdentity>emptySet()) {
            this.identities = new LinkedHashSet<>();
        }
        this.identities.add(identity);
    }

    public void removeIdentity(UserIdentity identity) {
        if (identity == null || this.identities == null || this.identities.isEmpty())
            return;
        this.identities.remove(identity);
    }

    // ---------- Helpers rôles ----------
    public List<String> getRolesList() {
        if (roles == null || roles.isBlank())
            return List.of();
        return Arrays.stream(roles.split(",")).map(String::trim).toList();
    }

    public void setRolesList(List<String> rolesList) {
        this.roles = (rolesList == null || rolesList.isEmpty()) ? null : String.join(",", rolesList);
    }

    private static String normalizeRole(String role) {
        if (role == null)
            return null;
        String r = role.trim().toUpperCase();
        if (!r.startsWith("ROLE_"))
            r = "ROLE_" + r;
        return r;
    }

    public boolean hasRole(String role) {
        String target = normalizeRole(role);
        if (target == null || target.isBlank())
            return false;
        for (String r : getRolesList()) {
            if (normalizeRole(r).equals(target))
                return true;
        }
        return false;
    }

    public boolean hasAnyRole(String... roles) {
        if (roles == null || roles.length == 0)
            return false;
        for (String role : roles)
            if (hasRole(role))
                return true;
        return false;
    }

    public boolean hasAllRoles(String... roles) {
        if (roles == null || roles.length == 0)
            return false;
        for (String role : roles)
            if (!hasRole(role))
                return false;
        return true;
    }

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    // ---------- Builder ----------
    public static class Builder {
        private Long id;
        private UUID publicId;
        private String displayName;
        private SrsAlgorithm srsAlgorithm = SrsAlgorithm.SM2;
        private String roles;
        private Boolean active;

        private int authVersion;
        private LocalDateTime authUpdatedAt;

        private List<UserEmail> emails;
        private Set<UserIdentity> identities;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withPublicId(UUID publicId) {
            this.publicId = publicId;
            return this;
        }

        public Builder withDisplayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder withSrsAlgorithm(SrsAlgorithm srsAlgorithm) {
            this.srsAlgorithm = srsAlgorithm;
            return this;
        }

        public Builder withRoles(String roles) {
            this.roles = roles;
            return this;
        }

        public Builder withActive(Boolean active) {
            this.active = active;
            return this;
        }

        public Builder withAuthVersion(int authVersion) {
            this.authVersion = authVersion;
            return this;
        }

        public Builder withAuthUpdatedAt(LocalDateTime authUpdatedAt) {
            this.authUpdatedAt = authUpdatedAt;
            return this;
        }

        public Builder withEmails(List<UserEmail> emails) {
            this.emails = emails;
            return this;
        }

        public Builder withIdentities(Set<UserIdentity> identities) {
            this.identities = identities;
            return this;
        }

        /** Optionnel : compat si tu passes encore des List. */
        public Builder withIdentitiesFromList(List<UserIdentity> identities) {
            this.identities = (identities != null) ? new LinkedHashSet<>(identities) : null;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof User that))
            return false;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", publicId=" + (publicId != null ? publicId : "null") +
                ", displayName='" + displayName + '\'' +
                ", srsAlgorithm=" + srsAlgorithm +
                ", roles='" + roles + '\'' +
                ", active=" + active +
                ", authVersion=" + authVersion +
                ", authUpdatedAt=" + authUpdatedAt +
                ", primaryEmail='" + getPrimaryEmailValue() + '\'' +
                ", emailsCount=" + (emails != null ? emails.size() : 0) +
                ", identitiesCount=" + (identities != null ? identities.size() : 0) +
                '}';
    }
}
