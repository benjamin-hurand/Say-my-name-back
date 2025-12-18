package com.saymyname.security;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.saymyname.core.model.auth.User;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    // --- Authorities / rôles ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRolesList().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    // --- Accès pratiques ---
    public Long getId() {
        return user.getId();
    }

    public UUID getPublicId() {
        return user.getPublicId();
    }

    public User getUser() {
        return user;
    }

    // --- Credentials ---
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Identité “technique” vue par Spring Security.
     * On choisit l’UUID public (stable) plutôt qu’un email (mutables).
     */
    @Override
    public String getUsername() {
        if (user.getPublicId() != null) {
            return user.getPublicId().toString();
        }
        // Fallbacks de sécurité (ne devraient pas être nécessaires si publicId généré
        // partout)
        if (user.getId() != null) {
            return String.valueOf(user.getId());
        }
        String primaryEmail = user.getPrimaryEmailValue();
        if (primaryEmail != null && !primaryEmail.isBlank()) {
            return primaryEmail;
        }
        return "unknown";
    }

    // --- Flags de compte ---
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(user.isActive());
    }

    // --- Utilitaire ---
    public boolean hasRole(String role) {
        return getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }
}
