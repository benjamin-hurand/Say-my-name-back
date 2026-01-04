// src/main/java/com/saymyname/service/email/EmailVerificationService.java
package com.saymyname.service.email;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.events.email.EmailVerificationRequestedEvent;
import com.saymyname.core.model.auth.EmailVerificationChallenge;
import com.saymyname.core.model.auth.EmailVerificationConfirmation;
import com.saymyname.core.model.auth.UserEmail;
import com.saymyname.core.model.enums.EmailVerificationPurpose;
import com.saymyname.persistence.dao.auth.EmailVerificationTokenDao;
import com.saymyname.persistence.entity.EmailVerificationTokenEntity;

@Service
public class EmailVerificationService {

    private static final int TTL_MINUTES = 15;
    private static final int MAX_ATTEMPTS = 6;

    // resend policy
    private static final int MAX_RESENDS = 3;
    private static final int RESEND_COOLDOWN_SECONDS = 30;

    private final EmailVerificationTokenDao tokenDao;
    private final EmailVerificationCrypto crypto;
    private final UserEmailService userEmailService;
    private final ApplicationEventPublisher publisher;

    public EmailVerificationService(
            EmailVerificationTokenDao tokenDao,
            EmailVerificationCrypto crypto,
            UserEmailService userEmailService,
            ApplicationEventPublisher publisher) {
        this.tokenDao = tokenDao;
        this.crypto = crypto;
        this.userEmailService = userEmailService;
        this.publisher = publisher;
    }

    @Transactional
    public EmailVerificationChallenge requestRegisterEmailVerification(Long userId, String email) {
        if (userId == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId requis");
        String normalized = normalizeEmailOrThrow(email);

        LocalDateTime now = LocalDateTime.now();
        EmailVerificationPurpose purpose = EmailVerificationPurpose.REGISTER_EMAIL;

        // si déjà vérifié => alreadyVerified
        boolean alreadyVerified = userEmailService.findUserEmailForUserIgnoreCase(userId, normalized)
                .map(ue -> ue.getVerifiedAt() != null)
                .orElse(false);
        if (alreadyVerified) {
            return EmailVerificationChallenge.alreadyVerified(normalized);
        }

        // challenge actif ?
        Optional<EmailVerificationTokenEntity> activeOpt = tokenDao.findLatestActive(userId, normalized, purpose, now);
        if (activeOpt.isPresent()) {
            EmailVerificationTokenEntity t = activeOpt.get();
            if (t.getAttempts() < MAX_ATTEMPTS) {
                resendOtpOnExistingTokenOrThrow(t, now);
                int ttlLeft = ttlMinutesLeft(now, t.getExpiresAt());
                return EmailVerificationChallenge.otp(normalized, true, t.getPublicId(), ttlLeft);
            }
        }

        // créer nouveau challenge
        UUID verificationId = UUID.randomUUID();
        String code = crypto.newCode6();

        EmailVerificationTokenEntity t = new EmailVerificationTokenEntity();
        t.setPublicId(verificationId);
        t.setUserId(userId);
        t.setEmail(normalized);
        t.setTokenHash(crypto.tokenHash(verificationId.toString()));
        t.setPurpose(purpose);
        t.setMakePrimaryNow(true); // ici c’est forcément le primaire
        t.setCodeHashPhc(crypto.hashCodePhc(code));
        t.setAttempts(0);
        t.setResendCount(0);
        t.setLastSentAt(now);
        t.setExpiresAt(now.plusMinutes(TTL_MINUTES));
        t.setConsumedAt(null);

        tokenDao.save(t);

        publisher.publishEvent(new EmailVerificationRequestedEvent(
                userId, normalized, verificationId.toString(), code, purpose.name()));

        return EmailVerificationChallenge.otp(normalized, true, verificationId, TTL_MINUTES);
    }

    @Transactional
    public Long confirmRegisterEmailVerification(String email, UUID verificationId, String code) {

        if (verificationId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "verificationId requis");
        }

        String normalizedEmail = normalizeEmailOrThrow(email);
        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code requis");
        }

        EmailVerificationTokenEntity t = tokenDao.findByPublicId(verificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge invalide"));

        if (t.getPurpose() != EmailVerificationPurpose.REGISTER_EMAIL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge invalide (purpose)");
        }
        if (t.getEmail() == null || !t.getEmail().equalsIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email ne correspond pas au challenge");
        }

        LocalDateTime now = LocalDateTime.now();

        if (t.getConsumedAt() != null) {
            throw new ResponseStatusException(HttpStatus.GONE, "Challenge déjà consommé");
        }
        if (t.getExpiresAt() != null && now.isAfter(t.getExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Challenge expiré");
        }
        if (t.getAttempts() >= MAX_ATTEMPTS) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Trop de tentatives");
        }

        boolean ok = crypto.matchesCode(code.trim(), t.getCodeHashPhc());
        if (!ok) {
            tokenDao.incrementAttempts(t.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code invalide");
        }

        Long userId = t.getUserId();
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge invalide (userId)");
        }

        userEmailService.markVerifiedOrThrow(userId, normalizedEmail);
        tokenDao.markConsumed(t.getId(), now);

        return userId;
    }

    /**
     * ENDPOINT (register resend): /api/auth/register/resend
     * - renvoie un OTP pour un challenge existant (verificationId + email)
     */
    @Transactional
    public EmailVerificationChallenge resendRegisterEmailOtp(String email, UUID verificationId) {

        if (verificationId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "verificationId requis");
        }

        String normalizedEmail = normalizeEmailOrThrow(email);

        EmailVerificationTokenEntity t = tokenDao.findByPublicId(verificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge invalide"));

        if (t.getPurpose() != EmailVerificationPurpose.REGISTER_EMAIL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge invalide (purpose)");
        }
        if (t.getEmail() == null || !t.getEmail().equalsIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email ne correspond pas au challenge");
        }

        LocalDateTime now = LocalDateTime.now();

        if (t.getConsumedAt() != null) {
            throw new ResponseStatusException(HttpStatus.GONE, "Challenge déjà consommé");
        }
        if (t.getExpiresAt() != null && now.isAfter(t.getExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Challenge expiré");
        }

        resendOtpOnExistingTokenOrThrow(t, now);
        int ttlLeft = ttlMinutesLeft(now, t.getExpiresAt());

        return EmailVerificationChallenge.otp(normalizedEmail, true, verificationId, ttlLeft);
    }

    /**
     * ENDPOINT #1 : /api/auth/emails/add
     *
     * Comportement:
     * - si email déjà attaché + vérifié => alreadyVerified (option: switch primary)
     * - sinon:
     * - si challenge actif: resend implicite (rotate code) sous conditions
     * quota/cooldown
     * - sinon: crée challenge + envoie OTP
     */
    @Transactional
    public EmailVerificationChallenge requestAddEmailVerification(Long userId, String email, boolean makePrimaryNow) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié");
        }

        String normalized = normalizeEmailOrThrow(email);

        // email déjà utilisé par un autre user
        if (userEmailService.isEmailTakenByAnotherUser(userId, normalized)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé");
        }

        Optional<UserEmail> existing = userEmailService.findUserEmailForUserIgnoreCase(userId, normalized);
        boolean alreadyAttached = existing.isPresent();
        boolean alreadyVerified = existing.map(ue -> ue.getVerifiedAt() != null).orElse(false);

        // déjà vérifié -> pas de challenge
        if (alreadyAttached && alreadyVerified) {
            if (makePrimaryNow) {
                userEmailService.switchPrimary(userId, existing.get().getId());
            }
            return EmailVerificationChallenge.alreadyVerified(normalized);
        }

        LocalDateTime now = LocalDateTime.now();
        EmailVerificationPurpose purpose = EmailVerificationPurpose.ADD_EMAIL;

        // challenge actif ?
        Optional<EmailVerificationTokenEntity> activeOpt = tokenDao.findLatestActive(userId, normalized, purpose, now);
        if (activeOpt.isPresent()) {
            EmailVerificationTokenEntity t = activeOpt.get();

            // trop de tentatives => on ne réutilise pas, on force un nouveau
            if (t.getAttempts() < MAX_ATTEMPTS) {

                // si user coche makePrimaryNow maintenant, on le mémorise
                if (makePrimaryNow && !t.isMakePrimaryNow()) {
                    tokenDao.enableMakePrimaryNow(t.getId());
                    t.setMakePrimaryNow(true); // pour cohérence du retour
                }

                // resend implicite (rotate OTP) sous policy
                resendOtpOnExistingTokenOrThrow(t, now);

                int ttlLeft = ttlMinutesLeft(now, t.getExpiresAt());
                return EmailVerificationChallenge.otp(normalized, alreadyAttached, t.getPublicId(), ttlLeft);
            }
        }

        // créer nouveau challenge
        UUID verificationId = UUID.randomUUID();
        String code = crypto.newCode6();

        EmailVerificationTokenEntity t = new EmailVerificationTokenEntity();
        t.setPublicId(verificationId);
        t.setUserId(userId);
        t.setEmail(normalized);

        // token_hash NOT NULL
        t.setTokenHash(crypto.tokenHash(verificationId.toString()));

        t.setPurpose(purpose);
        t.setMakePrimaryNow(makePrimaryNow);

        t.setCodeHashPhc(crypto.hashCodePhc(code));
        t.setAttempts(0);

        // resend init
        t.setResendCount(0);
        t.setLastSentAt(now);

        t.setExpiresAt(now.plusMinutes(TTL_MINUTES));
        t.setConsumedAt(null);

        tokenDao.save(t);

        // envoi OTP
        publisher.publishEvent(new EmailVerificationRequestedEvent(
                userId,
                normalized,
                verificationId.toString(),
                code,
                purpose.name()));

        return EmailVerificationChallenge.otp(normalized, alreadyAttached, verificationId, TTL_MINUTES);
    }

    /**
     * ENDPOINT #2 : /api/auth/emails/confirm
     */
    @Transactional
    public EmailVerificationConfirmation confirmAddEmailVerification(
            Long userId,
            String email,
            UUID verificationId,
            String code) {

        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié");
        }
        if (verificationId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "verificationId requis");
        }

        String normalizedEmail = normalizeEmailOrThrow(email);

        if (code == null || code.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code requis");
        }

        EmailVerificationTokenEntity t = tokenDao.findByPublicId(verificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge invalide"));

        if (!userId.equals(t.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Challenge non autorisé");
        }

        if (t.getPurpose() != EmailVerificationPurpose.ADD_EMAIL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge invalide (purpose)");
        }

        if (t.getEmail() == null || !t.getEmail().equalsIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email ne correspond pas au challenge");
        }

        LocalDateTime now = LocalDateTime.now();

        if (t.getConsumedAt() != null) {
            throw new ResponseStatusException(HttpStatus.GONE, "Challenge déjà consommé");
        }
        if (t.getExpiresAt() != null && now.isAfter(t.getExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Challenge expiré");
        }
        if (t.getAttempts() >= MAX_ATTEMPTS) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Trop de tentatives");
        }

        boolean ok = crypto.matchesCode(code.trim(), t.getCodeHashPhc());
        if (!ok) {
            tokenDao.incrementAttempts(t.getId());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code invalide");
        }

        // safety: email pas pris entre temps
        if (userEmailService.isEmailTakenByAnotherUser(userId, t.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email déjà utilisé");
        }

        // upsert + verify
        UserEmail verified = userEmailService.upsertAndVerifyEmail(userId, t.getEmail());

        boolean primaryChanged = false;
        if (t.isMakePrimaryNow()) {
            userEmailService.switchPrimary(userId, verified.getId());
            primaryChanged = true;
        }

        tokenDao.markConsumed(t.getId(), now);

        return new EmailVerificationConfirmation(verified, primaryChanged);
    }

    /**
     * ENDPOINT #3 (flow resend explicite): /api/auth/emails/resend
     * - renvoie un OTP pour un challenge existant (verificationId + email)
     */
    @Transactional
    public EmailVerificationChallenge resendAddEmailOtp(Long userId, String email, UUID verificationId) {
        if (userId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifié");
        }
        if (verificationId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "verificationId requis");
        }

        String normalizedEmail = normalizeEmailOrThrow(email);

        EmailVerificationTokenEntity t = tokenDao.findByPublicId(verificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge invalide"));

        if (!userId.equals(t.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Challenge non autorisé");
        }
        if (t.getPurpose() != EmailVerificationPurpose.ADD_EMAIL) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Challenge invalide (purpose)");
        }
        if (t.getEmail() == null || !t.getEmail().equalsIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email ne correspond pas au challenge");
        }

        LocalDateTime now = LocalDateTime.now();

        if (t.getConsumedAt() != null) {
            throw new ResponseStatusException(HttpStatus.GONE, "Challenge déjà consommé");
        }
        if (t.getExpiresAt() != null && now.isAfter(t.getExpiresAt())) {
            throw new ResponseStatusException(HttpStatus.GONE, "Challenge expiré");
        }

        resendOtpOnExistingTokenOrThrow(t, now);

        boolean alreadyAttached = userEmailService.findUserEmailForUserIgnoreCase(userId, normalizedEmail).isPresent();
        int ttlLeft = ttlMinutesLeft(now, t.getExpiresAt());

        return EmailVerificationChallenge.otp(normalizedEmail, alreadyAttached, verificationId, ttlLeft);
    }

    // ----------------- resend helper -----------------

    private void resendOtpOnExistingTokenOrThrow(EmailVerificationTokenEntity t, LocalDateTime now) {
        if (t.getResendCount() >= MAX_RESENDS) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Trop de renvois");
        }

        if (t.getLastSentAt() != null) {
            long seconds = Duration.between(t.getLastSentAt(), now).getSeconds();
            if (seconds < RESEND_COOLDOWN_SECONDS) {
                throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Veuillez patienter avant de renvoyer");
            }
        }

        String code = crypto.newCode6();
        String hash = crypto.hashCodePhc(code);

        boolean rotated = tokenDao.rotateCodeAndMarkResent(t.getId(), hash, now);
        if (!rotated) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Impossible de renvoyer (challenge modifié)");
        }

        publisher.publishEvent(new EmailVerificationRequestedEvent(
                t.getUserId(),
                t.getEmail(),
                t.getPublicId().toString(),
                code,
                t.getPurpose().name()));
    }

    // ----------------- helpers -----------------

    private static String normalizeEmailOrThrow(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email requis");
        }
        return email.trim();
    }

    private static int ttlMinutesLeft(LocalDateTime now, LocalDateTime expiresAt) {
        if (expiresAt == null)
            return TTL_MINUTES;
        long seconds = Duration.between(now, expiresAt).getSeconds();
        if (seconds <= 0)
            return 0;
        long mins = (seconds + 59) / 60; // ceil
        return (int) Math.min(Integer.MAX_VALUE, mins);
    }
}
