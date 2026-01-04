// src/main/java/com/saymyname/service/UserService.java
package com.saymyname.service;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    // ===================== Métier basique =====================

    public User save(User user) {
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

    public Optional<User> findByIdWithEmails(Long userId) {
        return userDao.findByIdWithGraph(userId);
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

    @Transactional
    public User updateDisplayName(User me, String newDisplayName) {
        if (me == null || me.getId() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non trouvé");
        }

        String trimmed = newDisplayName == null ? "" : newDisplayName.trim();

        if (trimmed.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le nom du compte est requis");
        }
        if (trimmed.length() > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Le nom du compte est trop long (50 caractères max)");
        }

        if (trimmed.equals(me.getDisplayName())) {
            return me;
        }

        if (userDao.existsByDisplayNameIgnoreCase(trimmed)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ce nom de compte est déjà utilisé");
        }

        return userDao.updateDisplayName(me.getId(), trimmed);
    }

    public boolean hasVerifiedEmail(Long userId, String email) {
        return userDao.hasVerifiedEmail(userId, email);
    }

    // ===================== Auth invalidation =====================

    /**
     * Invalide globalement les sessions en bumpant users.auth_version (+ audit
     * auth_updated_at).
     * À appeler à chaque action security-sensitive (reset password, set local
     * password, etc.).
     */
    @Transactional
    public void bumpAuthVersionOrThrow(Long userId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId requis");
        }
        userDao.bumpAuthVersionOrThrow(userId);
    }

    // ===================== Sécurité (Spring Security) =====================

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        User user = userDao.findByPrincipal(identifier);
        return new CustomUserDetails(user);
    }

    public UserDetails loadUserById(Long id) {
        User user = userDao.findById(id);
        return new CustomUserDetails(user);
    }

    public UserDetails loadUserByPublicId(UUID publicId) {
        User user = userDao.findByPublicIdOrThrow(publicId);
        return new CustomUserDetails(user);
    }

    // ---------- Current user helpers ----------

    public Optional<User> getCurrentUser(Principal principal) {
        if (principal == null)
            return Optional.empty();

        if (principal instanceof Authentication auth) {
            Object p = auth.getPrincipal();

            if (p instanceof CustomUserDetails cud) {
                return Optional.ofNullable(cud.getUser());
            }

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

        if (principal instanceof CustomUserDetails cud) {
            Long id = cud.getId();
            if (id != null)
                return id;
        }

        if (principal instanceof org.springframework.security.core.userdetails.User ud) {
            try {
                UUID pub = UUID.fromString(ud.getUsername());
                return userDao.findIdByPublicId(pub)
                        .orElseThrow(() -> new UnauthorizedException("User id not found for publicId"));
            } catch (IllegalArgumentException ignore) {
                // legacy
            }
        }

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

    private Optional<User> findBySubjectFlexible(String subject) {
        if (subject == null || subject.isBlank())
            return Optional.empty();

        try {
            UUID publicId = UUID.fromString(subject);
            return Optional.of(userDao.findByPublicIdOrThrow(publicId));
        } catch (IllegalArgumentException ignored) {
            // not a UUID
        }

        try {
            return userDao.findOptionalByEmailIgnoreCase(subject);
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
