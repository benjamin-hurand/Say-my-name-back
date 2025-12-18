// src/main/java/com/saymyname/service/UserService.java
package com.saymyname.service;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.exception.common.UnauthorizedException;
import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.SrsAlgorithm;
import com.saymyname.persistence.dao.UserDao;
import com.saymyname.security.CustomUserDetails;

import jakarta.transaction.Transactional;

@Service
public class UserService implements UserDetailsService {

    private final UserDao userDao;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    // ===================== Métier basique =====================

    public User save(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userDao.save(user);
    }

    public User setActive(User user) {
        user.setActive(true);
        return userDao.save(user);
    }

    public Boolean checkIfAccountExistsWithEmail(String email) {
        return userDao.checkIfEmailExists(email);
    }

    public User findById(Long id) {
        return userDao.findById(id);
    }

    /** Lookup email insensible à la casse (Optional). */
    public Optional<User> findOptionalByEmailIgnoreCase(String email) {
        return userDao.findOptionalByEmailIgnoreCase(email);
    }

    /** Lookup email insensible à la casse (throws 401 si introuvable). */
    public User findByEmailIgnoreCaseOrThrow(String email) {
        return userDao.findOptionalByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur introuvable"));
    }

    // ===================== Sécurité (Spring Security) =====================

    /**
     * Requise par Spring Security.
     * On interprète le "username" passé par Spring comme:
     * - d'abord un UUID (publicId) venant du subject du JWT (nouveau flux),
     * - sinon un e-mail (legacy).
     */
    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = userDao.findByPrincipal(identifier); // UUID publicId -> user, sinon email -> user
        return new CustomUserDetails(user);
    }

    /** Compat anciens tokens: subject = userId (Long). */
    public UserDetails loadUserById(Long id) {
        User user = userDao.findById(id);
        return new CustomUserDetails(user);
    }

    /** ✅ Nouveau flux: subject = publicId (UUID). */
    public UserDetails loadUserByPublicId(UUID publicId) {
        User user = userDao.findByPublicIdOrThrow(publicId);
        return new CustomUserDetails(user);
    }

    // ---------- Récupération du user courant via Principal / SecurityContext
    // ----------

    public Optional<User> getCurrentUser(Principal principal) {
        if (principal == null)
            return Optional.empty();

        if (principal instanceof Authentication auth) {
            Object p = auth.getPrincipal();

            // Chemin rapide si CustomUserDetails
            if (p instanceof CustomUserDetails cud) {
                return Optional.ofNullable(cud.getUser());
            }

            // Sinon, auth.getName() = subject → UUID (nouveau) ou email (legacy)
            String name = safeTrim(auth.getName());
            return findBySubjectFlexible(name);
        }

        String name = safeTrim(principal.getName());
        return findBySubjectFlexible(name);
    }

    public User getCurrentUserOrThrow(Principal principal) {
        return getCurrentUser(principal)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non trouvé"));
    }

    public Optional<User> getCurrentAuthenticatedUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null)
            return Optional.empty();

        Object p = auth.getPrincipal();
        if (p instanceof CustomUserDetails cud) {
            return Optional.ofNullable(cud.getUser());
        }

        String name = safeTrim(auth.getName());
        return findBySubjectFlexible(name);
    }

    public User getCurrentAuthenticatedUserOrThrow() {
        return getCurrentAuthenticatedUser()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non trouvé"));
    }

    public Long getCurrentIdOrThrow() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not authenticated");
        }

        Object principal = auth.getPrincipal();

        // Chemin rapide : notre CustomUserDetails expose l'id
        if (principal instanceof CustomUserDetails cud) {
            Long id = cud.getId();
            if (id != null)
                return id;
        }

        // Fallback : subject = publicId (UUID) sur un Principal "User" Spring
        if (principal instanceof org.springframework.security.core.userdetails.User ud) {
            try {
                UUID pub = UUID.fromString(ud.getUsername());
                return userDao.findIdByPublicId(pub)
                        .orElseThrow(() -> new UnauthorizedException("User id not found for publicId"));
            } catch (IllegalArgumentException ignore) {
                // subject non-UUID (legacy) → on retombe sur la charge complète
            }
        }

        // Dernier recours
        return getCurrentAuthenticatedUserOrThrow().getId();
    }

    @Transactional
    public User updateSrsAlgorithm(User me, SrsAlgorithm newAlgo) {
        if (newAlgo == null || newAlgo.equals(me.getSrsAlgorithm())) {
            return me;
        }
        return userDao.updateSrsAlgorithm(me, newAlgo);
    }

    // ===================== Helpers privés =====================

    private static String safeTrim(String s) {
        return s == null ? null : s.trim();
    }

    /**
     * Essaie d'interpréter le subject comme UUID (nouveau flux),
     * sinon retombe sur e-mail (legacy).
     */
    private Optional<User> findBySubjectFlexible(String subject) {
        if (subject == null || subject.isBlank())
            return Optional.empty();

        // Nouveau: subject = UUID publicId
        try {
            UUID publicId = UUID.fromString(subject);
            return Optional.of(userDao.findByPublicIdOrThrow(publicId));
        } catch (IllegalArgumentException ignored) {
            // pas un UUID → fallback legacy (e-mail)
        }

        try {
            return userDao.findOptionalByEmailIgnoreCase(subject);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
