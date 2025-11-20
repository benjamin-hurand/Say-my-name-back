package com.saymyname.core.model.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import com.saymyname.core.model.enums.SrsAlgorithm;

public class User {

    private Long id;
    /** Identifiant public stable (UUID), exposable côté front. */
    private UUID publicId;

    private String username;
    private SrsAlgorithm srsAlgorithm = SrsAlgorithm.SM2;
    private String password;
    /** Version de mot de passe pour invalider les JWT existants. */
    private int passwordVersion = 0;
    /** Chaîne CSV de rôles, ex: "ROLE_USER,ROLE_ADMIN". */
    private String roles;
    private Boolean active;

    /** Emails liés au compte (jamais null ; liste vide par défaut). */
    private List<UserEmail> emails = Collections.emptyList();

    // ---------- Constructeurs ----------
    public User() {
    }

    private User(Builder b) {
        this.id = b.id;
        this.publicId = b.publicId;
        this.username = b.username;
        this.srsAlgorithm = b.srsAlgorithm != null ? b.srsAlgorithm : SrsAlgorithm.SM2;
        this.password = b.password;
        this.passwordVersion = b.passwordVersion;
        this.roles = b.roles;
        this.active = b.active;
        setEmails(b.emails); // copie défensive et jamais null
    }

    // ---------- Getters ----------
    public Long getId() {
        return id;
    }

    public UUID getPublicId() {
        return publicId;
    }

    public String getUsername() {
        return username;
    }

    public SrsAlgorithm getSrsAlgorithm() {
        return srsAlgorithm;
    }

    public String getPassword() {
        return password;
    }

    public int getPasswordVersion() {
        return passwordVersion;
    }

    public String getRoles() {
        return roles;
    }

    public Boolean isActive() {
        return active;
    }

    public List<UserEmail> getEmails() {
        return emails;
    }

    /**
     * Rétro-compat : renvoie la valeur de l'email primaire, ou null si absent.
     * Préférer getPrimaryEmailValue() / getPrimaryEmail() pour être explicite.
     */
    public String getEmail() {
        return getPrimaryEmailValue();
    }

    /**
     * Valeur de l'email primaire (ou null).
     */
    public String getPrimaryEmailValue() {
        if (emails == null)
            return null;
        for (UserEmail e : emails) {
            if (Boolean.TRUE.equals(e.isPrimary())) {
                return e.getEmail();
            }
        }
        return null;
    }

    /**
     * Email primaire sous forme d'objet (Optional).
     */
    public Optional<UserEmail> getPrimaryEmail() {
        if (emails == null)
            return Optional.empty();
        return emails.stream().filter(e -> Boolean.TRUE.equals(e.isPrimary())).findFirst();
    }

    // ---------- Setters ----------
    public void setId(Long id) {
        this.id = id;
    }

    public void setPublicId(UUID publicId) {
        this.publicId = publicId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setSrsAlgorithm(SrsAlgorithm srsAlgorithm) {
        this.srsAlgorithm = srsAlgorithm;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setPasswordVersion(int passwordVersion) {
        this.passwordVersion = passwordVersion;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    /** Copie défensive et jamais null. */
    public void setEmails(List<UserEmail> emails) {
        this.emails = (emails != null) ? new ArrayList<>(emails) : Collections.emptyList();
    }

    /** Ajoute un email (si null, ignoré). */
    public void addEmail(UserEmail email) {
        if (email == null)
            return;
        if (this.emails == null || this.emails == Collections.<UserEmail>emptyList()) {
            this.emails = new ArrayList<>();
        }
        this.emails.add(email);
    }

    /** Retire un email (si null, ignoré). */
    public void removeEmail(UserEmail email) {
        if (email == null || this.emails == null || this.emails.isEmpty())
            return;
        this.emails.remove(email);
    }

    // ---------- Helpers rôles ----------
    /** Retourne la liste des rôles à partir de la chaîne CSV. */
    public List<String> getRolesList() {
        if (roles == null || roles.isBlank())
            return List.of();
        return Arrays.stream(roles.split(",")).map(String::trim).toList();
    }

    /** Définit les rôles à partir d'une liste (concaténée en CSV). */
    public void setRolesList(List<String> rolesList) {
        this.roles = (rolesList == null || rolesList.isEmpty())
                ? null
                : String.join(",", rolesList);
    }

    /** Normalise un rôle (ajoute ROLE_ si manquant + uppercase). */
    private static String normalizeRole(String role) {
        if (role == null)
            return null;
        String r = role.trim().toUpperCase();
        if (!r.startsWith("ROLE_"))
            r = "ROLE_" + r;
        return r;
    }

    /**
     * Vrai si l'utilisateur possède le rôle donné (ex: "ADMIN" ou "ROLE_ADMIN").
     */
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

    /** Vrai si l'utilisateur possède AU MOINS un des rôles listés. */
    public boolean hasAnyRole(String... roles) {
        if (roles == null || roles.length == 0)
            return false;
        for (String role : roles) {
            if (hasRole(role))
                return true;
        }
        return false;
    }

    /** Vrai si l'utilisateur possède TOUS les rôles listés. */
    public boolean hasAllRoles(String... roles) {
        if (roles == null || roles.length == 0)
            return false;
        for (String role : roles) {
            if (!hasRole(role))
                return false;
        }
        return true;
    }

    /** Raccourci : est-ce un admin ? */
    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }

    // ---------- Builder ----------
    public static class Builder {
        private Long id;
        private UUID publicId;
        private String username;
        private SrsAlgorithm srsAlgorithm = SrsAlgorithm.SM2;
        private String password;
        private int passwordVersion = 0;
        private String roles;
        private Boolean active;
        private List<UserEmail> emails;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withPublicId(UUID publicId) {
            this.publicId = publicId;
            return this;
        }

        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder withSrsAlgorithm(SrsAlgorithm srsAlgorithm) {
            this.srsAlgorithm = srsAlgorithm;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public Builder withPasswordVersion(int passwordVersion) {
            this.passwordVersion = passwordVersion;
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

        public Builder withEmails(List<UserEmail> emails) {
            this.emails = emails;
            return this;
        }

        public User build() {
            return new User(this);
        }
    }

    // ---------- equals/hashCode (id only) ----------
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

    // ---------- toString (sans password) ----------
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", publicId=" + (publicId != null ? publicId : "null") +
                ", username='" + username + '\'' +
                ", srsAlgorithm=" + srsAlgorithm +
                ", passwordVersion=" + passwordVersion +
                ", roles='" + roles + '\'' +
                ", active=" + active +
                ", primaryEmail='" + getPrimaryEmailValue() + '\'' +
                ", emailsCount=" + (emails != null ? emails.size() : 0) +
                '}';
    }
}
