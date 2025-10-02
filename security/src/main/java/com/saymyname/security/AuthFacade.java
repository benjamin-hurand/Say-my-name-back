package com.saymyname.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthFacade {
    public Long currentUserId() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null)
            return null;
        Object p = a.getPrincipal();
        // adapte selon ton UserDetails/JWT ; ici on suppose un principal avec getId()
        try {
            return (Long) p.getClass().getMethod("getId").invoke(p);
        } catch (Exception ignore) {
            return null;
        }
    }

    public boolean hasGlobalRole(String role) { // "SUPER_ADMIN", "ADMIN", "USER"
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null)
            return false;
        for (GrantedAuthority ga : a.getAuthorities()) {
            if (("ROLE_" + role).equals(ga.getAuthority()))
                return true;
        }
        return false;
    }
}
