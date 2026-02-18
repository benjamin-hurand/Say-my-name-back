package com.saymyname.core.model.auth;

import com.saymyname.core.model.enums.AuthProvider;
import com.saymyname.core.model.enums.SrsAlgorithm;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class User {

    Long id;
    byte[] publicId;
    String displayName;
    @Builder.Default
    SrsAlgorithm srsAlgorithm = SrsAlgorithm.SM2;
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
        for (UserEmail email : emails) {
            if (email.isPrimary()) {
                return email.getEmail();
            }
        }
        return null;
    }

    public Optional<UserEmail> getPrimaryEmail() {
        return emails.stream().filter(UserEmail::isPrimary).findFirst();
    }

    public boolean hasAuthProvider(AuthProvider provider) {
        if (provider == null) {
            return false;
        }
        return identities.stream().anyMatch(i -> i.isEnabled() && provider == i.getProvider());
    }

    public boolean hasLocalPassword() {
        return identities.stream().anyMatch(i ->
                i.isEnabled()
                        && i.getProvider() == AuthProvider.LOCAL
                        && i.getPasswordHash() != null
                        && !i.getPasswordHash().isBlank());
    }

    public Optional<UserIdentity> getLocalIdentity() {
        return identities.stream()
                .filter(i -> i.isEnabled() && i.getProvider() == AuthProvider.LOCAL)
                .findFirst();
    }

    public List<String> getRolesList() {
        if (roles == null || roles.isBlank()) {
            return List.of();
        }
        return Arrays.stream(roles.split(",")).map(String::trim).toList();
    }

    private static String normalizeRole(String role) {
        if (role == null) {
            return null;
        }
        String normalized = role.trim().toUpperCase();
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        return normalized;
    }

    public boolean hasRole(String role) {
        String target = normalizeRole(role);
        if (target == null || target.isBlank()) {
            return false;
        }
        return getRolesList().stream().map(User::normalizeRole).anyMatch(target::equals);
    }

    public boolean hasAnyRole(String... rolesToCheck) {
        if (rolesToCheck == null || rolesToCheck.length == 0) {
            return false;
        }
        for (String role : rolesToCheck) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAllRoles(String... rolesToCheck) {
        if (rolesToCheck == null || rolesToCheck.length == 0) {
            return false;
        }
        for (String role : rolesToCheck) {
            if (!hasRole(role)) {
                return false;
            }
        }
        return true;
    }

    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }
}
