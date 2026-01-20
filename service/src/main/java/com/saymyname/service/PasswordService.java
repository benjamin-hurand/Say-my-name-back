// src/main/java/com/saymyname/service/PasswordService.java
package com.saymyname.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.model.auth.PasswordResetToken;
import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.auth.UserEmail;
import com.saymyname.persistence.dao.UserDao;
import com.saymyname.persistence.dao.UserEmailDao;
import com.saymyname.persistence.dao.auth.PasswordResetTokenDao;
import com.saymyname.service.auth.AuthInvalidationService;
import com.saymyname.service.auth.UserIdentityService;
import com.saymyname.service.port.Mailer;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class PasswordService {

    private final UserDao userDao;
    private final UserEmailDao userEmailDao;
    private final PasswordResetTokenDao tokenDao;
    private final Mailer mailer;
    private final String frontendBaseUrl;

    private final UserIdentityService userIdentityService;
    private final AuthInvalidationService authInvalidationService;

    private static final java.security.SecureRandom SECURE_RANDOM = new java.security.SecureRandom();

    public PasswordService(
            UserDao userDao,
            UserEmailDao userEmailDao,
            PasswordResetTokenDao tokenDao,
            Mailer mailer,
            UserIdentityService userIdentityService,
            AuthInvalidationService authInvalidationService,
            @Value("${app.frontend.base-url:http://localhost:5173}") String frontendBaseUrl) {
        this.userDao = userDao;
        this.userEmailDao = userEmailDao;
        this.tokenDao = tokenDao;
        this.mailer = mailer;
        this.userIdentityService = userIdentityService;
        this.authInvalidationService = authInvalidationService;
        this.frontendBaseUrl = frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                : frontendBaseUrl;
    }

    /** 1) Mot de passe oublié : émettre un lien (silencieux si email inconnu). */
    @Transactional
    public void issueResetToken(String email, HttpServletRequest req) {
        String input = email == null ? null : email.trim();
        if (input == null || input.isBlank())
            return;

        Optional<UserEmail> ueOpt = userEmailDao.findRecoveryEligibleByEmailIgnoreCase(input);
        if (ueOpt.isEmpty())
            return;

        Long userId = ueOpt.get().getUserId();
        User user = userDao.findById(userId);

        String raw = randomUrlSafeToken(32);
        String hash = sha256Base64(raw);

        PasswordResetToken token = new PasswordResetToken.Builder()
                .withUserId(user.getId())
                .withTokenHash(hash)
                .withExpiresAt(OffsetDateTime.now().plusMinutes(30))
                .withCreatedIp(clientIp(req))
                .withUserAgent(userAgent(req))
                .build();

        tokenDao.save(token);

        String link = frontendBaseUrl + "/reset-password?token=" + raw;
        mailer.sendPasswordResetEmail(input, link);
    }

    /**
     * 2) Reset via lien : consommer le token et définir le nouveau mdp
     * (crée/replace LOCAL identity).
     */
    @Transactional
    public void resetWithToken(String rawToken, String newPassword) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token invalide");
        }

        String hash = sha256Base64(rawToken);
        PasswordResetToken token = tokenDao.findActiveByHash(hash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token invalide"));

        if (token.getExpiresAt() == null || OffsetDateTime.now().isAfter(token.getExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token expiré");
        }

        User user = userDao.findById(token.getUserId());
        ensurePolicy(newPassword, user);

        // 1) Set LOCAL password (user_identities)
        userIdentityService.setLocalPassword(user.getId(), newPassword);

        // 2) Consume this reset token + invalidate all reset tokens for user
        tokenDao.markUsed(token.getId());
        tokenDao.invalidateAllForUser(user.getId());

        // 3) Invalidate sessions (refresh revoke + bump auth_version)
        authInvalidationService.invalidateAllSessions(user.getId(), "PASSWORD_RESET");

        // 4) Notify primary email
        userEmailDao.findPrimaryEmailAddress(user.getId())
                .ifPresent(primary -> mailer.sendPasswordChangedInfoEmail(primary));
    }

    // ---------- helpers ----------
    private static String randomUrlSafeToken(int bytes) {
        byte[] buf = new byte[bytes];
        SECURE_RANDOM.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }

    private static String sha256Base64(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return Base64.getEncoder().encodeToString(md.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static String clientIp(HttpServletRequest req) {
        String xf = req.getHeader("X-Forwarded-For");
        return xf != null ? xf.split(",")[0].trim() : req.getRemoteAddr();
    }

    private static String userAgent(HttpServletRequest req) {
        return Optional.ofNullable(req.getHeader("User-Agent")).orElse("n/a");
    }

    private void ensurePolicy(String password, User user) {
        if (password == null || password.length() < 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mot de passe trop court (≥12)");
        }
        List<String> emails = userEmailDao.listLoginAllowedEmails(user.getId());
        if (emails == null || emails.isEmpty())
            return;

        String pwLower = password.toLowerCase();
        for (String em : emails) {
            int at = em.indexOf('@');
            String local = at > 0 ? em.substring(0, at).toLowerCase() : em.toLowerCase();
            if (pwLower.contains(local)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Évite d'utiliser ton email dans le mot de passe");
            }
        }
    }
}
