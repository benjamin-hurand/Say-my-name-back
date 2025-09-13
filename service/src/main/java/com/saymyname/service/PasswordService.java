// com.saymyname.service.PasswordService
package com.saymyname.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Optional;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.model.auth.PasswordResetToken;
import com.saymyname.core.model.auth.User;
import com.saymyname.persistence.dao.UserDao;
import com.saymyname.persistence.dao.auth.PasswordResetTokenDao;
import com.saymyname.service.port.Mailer;

@Service
public class PasswordService {

    private final UserDao userDao;
    private final PasswordResetTokenDao tokenDao;
    private final PasswordEncoder encoder;
    private final Mailer mailer;
    private final String frontendBaseUrl; // <-- injecté depuis app.frontend.base-url

    private static final java.security.SecureRandom SECURE_RANDOM = new java.security.SecureRandom();

    public PasswordService(UserDao userDao,
            PasswordResetTokenDao tokenDao,
            PasswordEncoder encoder,
            Mailer mailer,
            @Value("${app.frontend.base-url:http://localhost:5173}") String frontendBaseUrl) {
        this.userDao = userDao;
        this.tokenDao = tokenDao;
        this.encoder = encoder;
        this.mailer = mailer;
        this.frontendBaseUrl = frontendBaseUrl.endsWith("/")
                ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1)
                : frontendBaseUrl;
    }

    /** 1) Mot de passe oublié : émettre un lien (silencieux si email inconnu). */
    @Transactional
    public void issueResetToken(String email, HttpServletRequest req) {
        var uOpt = userDao.findOptionalByEmailIgnoreCase(email);
        if (uOpt.isEmpty())
            return; // ne révèle pas l’existence du compte

        User user = uOpt.get();
        String raw = randomUrlSafeToken(32); // 256 bits
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
        mailer.sendPasswordResetEmail(user.getEmail(), link);
    }

    /** 2) Reset via lien : consommer le token et définir le nouveau mdp. */
    @Transactional
    public void resetWithToken(String rawToken, String newPassword) {
        String hash = sha256Base64(rawToken);
        PasswordResetToken token = tokenDao.findActiveByHash(hash)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token invalide"));

        if (token.getExpiresAt() == null || OffsetDateTime.now().isAfter(token.getExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token expiré");
        }

        User user = userDao.findById(token.getUserId());
        ensurePolicy(newPassword, user);

        user.setPassword(encoder.encode(newPassword));
        user.setPasswordVersion(user.getPasswordVersion() + 1);
        userDao.save(user);

        tokenDao.markUsed(token.getId());
        mailer.sendPasswordChangedInfoEmail(user.getEmail());
    }

    /** 3) Changement depuis le profil (auth requis). */
    @Transactional
    public void changePassword(User user, String currentPassword, String newPassword) {
        if (!encoder.matches(currentPassword, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Mot de passe actuel incorrect");
        }
        ensurePolicy(newPassword, user);

        user.setPassword(encoder.encode(newPassword));
        user.setPasswordVersion(user.getPasswordVersion() + 1);
        userDao.save(user);
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
        if (user.getEmail() != null) {
            String local = user.getEmail().split("@")[0].toLowerCase();
            if (password.toLowerCase().contains(local)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Évite d'utiliser ton email dans le mdp");
            }
        }
    }
}
