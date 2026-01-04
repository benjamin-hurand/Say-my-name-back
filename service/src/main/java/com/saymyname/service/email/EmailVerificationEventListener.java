// src/main/java/com/saymyname/service/email/EmailVerificationEventListener.java
package com.saymyname.service.email;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.saymyname.core.events.email.EmailVerificationRequestedEvent;
import com.saymyname.core.model.enums.EmailVerificationPurpose;
import com.saymyname.service.port.Mailer;

@Component
public class EmailVerificationEventListener {

    private final Mailer mailer;

    /**
     * Optionnel: base URL front pour générer un lien (utile en prod).
     * Exemple: https://app.saymyname.com
     * En local: http://localhost:5173
     */
    private final String frontBaseUrl;

    public EmailVerificationEventListener(
            Mailer mailer,
            @Value("${app.front.base-url:http://localhost:5173}") String frontBaseUrl) {
        this.mailer = mailer;
        this.frontBaseUrl = frontBaseUrl;
    }

    @EventListener
    public void onEmailVerificationRequested(EmailVerificationRequestedEvent ev) {
        if (ev == null)
            return;

        EmailVerificationPurpose purpose = safePurpose(ev.purpose());

        // Lien optionnel: tu peux décider d'en faire quelque chose côté front
        // Exemple: /verify-email?email=...&verificationId=...
        // Ici rawToken = verificationId.toString() dans ton service.
        String link = buildLink(ev.email(), ev.rawToken());

        // Locale: si tu n'as pas encore i18n user-level, mets FR par défaut.
        Locale locale = Locale.FRENCH;

        mailer.sendEmailVerificationOtp(
                ev.email(),
                ev.code(),
                purpose,
                link,
                locale);
    }

    private String buildLink(String email, String verificationId) {
        if (frontBaseUrl == null || frontBaseUrl.isBlank())
            return null;
        if (verificationId == null || verificationId.isBlank())
            return null;

        // tu peux URL-encode plus tard si tu mets email en query param
        // (sinon, ne mets pas l'email dans le lien)
        return frontBaseUrl + "/auth/verify-email?verificationId=" + verificationId + "&email=" + email;
    }

    private static EmailVerificationPurpose safePurpose(String p) {
        if (p == null || p.isBlank())
            return EmailVerificationPurpose.ADD_EMAIL;
        try {
            return EmailVerificationPurpose.valueOf(p);
        } catch (Exception e) {
            return EmailVerificationPurpose.ADD_EMAIL;
        }
    }
}
