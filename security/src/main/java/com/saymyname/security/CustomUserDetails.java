package com.saymyname.security;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.AuthProvider;

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRolesList().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public Long getId() {
        return user.getId();
    }

    public UUID getPublicId() {
        return user.getPublicId();
    }

    public User getUser() {
        return user;
    }

    /**
     * IMPORTANT:
     * DaoAuthenticationProvider a besoin d'un "password" = hash.
     * Ici, on renvoie le password_hash de l'identité LOCAL (si elle existe),
     * sinon null (ce qui fera échouer un login local).
     */
    @Override
    public String getPassword() {
        if (user == null || user.getIdentities() == null) {
            return null;
        }
        return user.getIdentities().stream()
                .filter(i -> i != null && i.getProvider() == AuthProvider.LOCAL && Boolean.TRUE.equals(i.isEnabled()))
                .map(i -> i.getPasswordHash())
                .filter(h -> h != null && !h.isBlank())
                .findFirst()
                .orElse(null);
    }

    @Override
    public String getUsername() {
        if (user.getPublicId() != null) {
            return user.getPublicId().toString();
        }
        if (user.getId() != null) {
            return String.valueOf(user.getId());
        }
        String primaryEmail = user.getPrimaryEmailValue();
        if (primaryEmail != null && !primaryEmail.isBlank()) {
            return primaryEmail;
        }
        return "unknown";
    }

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

    public boolean hasRole(String role) {
        return getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(role));
    }
}
