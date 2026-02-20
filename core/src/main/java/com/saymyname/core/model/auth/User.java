// src/main/java/com/saymyname/core/model/auth/User.java
package com.saymyname.core.model.auth;

import com.saymyname.core.model.enums.AuthProvider;
import com.saymyname.core.model.enums.SrsAlgorithm;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class User {

    Long id;

    /** Identifiant public stable (UUID), exposable côté front. */
    UUID publicId;

    String displayName;

    @Builder.Default
    SrsAlgorithm srsAlgorithm = SrsAlgorithm.SM2;

    /** Chaîne CSV de rôles, ex: "ROLE_USER,ROLE_ADMIN". */
    String roles;

    @Builder.Default
    boolean active = false;

    int authVersion;
    Instant authUpdatedAt;

    @Builder.Default
    List<UserEmail> emails = List.of();

    @Builder.Default
    Set<UserIdentity> identities = Set.of();

    public String getEmail() {
        return getPrimaryEmailValue();
    }

    public String getPrimaryEmailValue() {
        if (emails == null)
            return null;
        for (UserEmail email : emails) {
            if (email != null && email.isPrimary())
                return email.getEmail();
        }
        return null;
    }

    public Optional<UserEmail> getPrimaryEmail() {
        if (emails == null)
            return Optional.empty();
        return emails.stream().filter(e -> e != null && e.isPrimary()).findFirst();
    }

    public boolean hasAuthProvider(AuthProvider provider) {
        if (provider == null || identities == null)
            return false;
        return identities.stream().anyMatch(i -> i != null && i.isEnabled() && provider == i.getProvider());
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

    public List<String> getRolesList() {
        if (roles == null || roles.isBlank())
            return List.of();
        return Arrays.stream(roles.split(",")).map(String::trim).toList();
    }

    private static String normalizeRole(String role) {
        if (role == null)
            return null;
        String normalized = role.trim().toUpperCase();
        if (!normalized.startsWith("ROLE_"))
            normalized = "ROLE_" + normalized;
        return normalized;
    }

    public boolean hasRole(String role) {
        String target = normalizeRole(role);
        if (target == null || target.isBlank())
            return false;
        return getRolesList().stream().map(User::normalizeRole).anyMatch(target::equals);
    }

    public boolean hasAnyRole(String... rolesToCheck) {
        if (rolesToCheck == null || rolesToCheck.length == 0)
            return false;
        for (String role : rolesToCheck)
            if (hasRole(role))
                return true;
        return false;
    }

    public boolean hasAllRoles(String... rolesToCheck) {
        if (rolesToCheck == null || rolesToCheck.length == 0)
            return false;
        for (String role : rolesToCheck)
            if (!hasRole(role))
                return false;
        return true;
    }

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }
}
