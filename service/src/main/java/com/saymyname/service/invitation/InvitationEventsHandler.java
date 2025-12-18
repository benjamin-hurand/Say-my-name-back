// src/main/java/com/saymyname/service/invitation/InvitationEventsHandler.java
package com.saymyname.service.invitation;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import com.saymyname.core.events.invitation.InvitationCreatedEvent;
import com.saymyname.core.model.enums.InvitationType;
import com.saymyname.persistence.dao.invitation.InvitationDao;
import com.saymyname.service.port.Mailer;

@Component
public class InvitationEventsHandler {

    private final InvitationDao invitationDao;
    private final Mailer mailer;
    private final ObjectMapper objectMapper;

    /** ex: https://app.saymyname.app */
    private final String frontendBaseUrl;
    /** ex: /invitation -> URL finale: {base}{route}?token=...&pin=... */
    private final String inviteRoute;

    public InvitationEventsHandler(
            InvitationDao invitationDao,
            Mailer mailer,
            ObjectMapper objectMapper,
            @Value("${app.frontend.base-url}") String frontendBaseUrl,
            @Value("${app.invite.route:/invitation}") String inviteRoute) {
        this.invitationDao = invitationDao;
        this.mailer = mailer;
        this.objectMapper = objectMapper;
        this.frontendBaseUrl = trimTrailingSlash(frontendBaseUrl);
        this.inviteRoute = inviteRoute.startsWith("/") ? inviteRoute : "/" + inviteRoute;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInvitationCreated(InvitationCreatedEvent evt) {
        invitationDao.findById(evt.invitationId()).ifPresent(inv -> {
            // On n'envoie un e-mail QUE pour les invitations de type EMAIL.
            // Les invitations SELF_SERVICE (lien / QR / code…) n'en déclenchent pas.
            if (inv.getType() != InvitationType.EMAIL) {
                return;
            }
            if (isBlank(evt.email())) {
                return;
            }

            String inviteUrl = buildInviteUrl(evt.rawToken(), evt.rawPin());
            InviteConstraints c = parseConstraints(evt.constraintsJson());

            mailer.sendInvitationEmail(
                    evt.email(),
                    inviteUrl,
                    inv.getRole(), // peut être null selon le modèle
                    c.locale(),
                    c.message());
        });
    }

    private String buildInviteUrl(String token, @Nullable String pin) {
        String t = URLEncoder.encode(token, StandardCharsets.UTF_8);
        String url = frontendBaseUrl + inviteRoute + "?token=" + t;
        if (!isBlank(pin)) {
            url += "&pin=" + URLEncoder.encode(pin, StandardCharsets.UTF_8);
        }
        return url;
    }

    /**
     * constraintsJson peut contenir :
     * - locale : "fr", "en", etc.
     * - message : texte libre pour personnaliser l'e-mail
     * (et éventuellement d'autres champs utilisés côté acceptByToken,
     * qui seront ignorés ici sans casser la lecture).
     */
    private InviteConstraints parseConstraints(@Nullable String json) {
        if (isBlank(json)) {
            return InviteConstraints.DEFAULT;
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            String lang = textOrNull(root.get("locale"));
            String msg = textOrNull(root.get("message"));

            Locale loc = (lang != null && !lang.isBlank())
                    ? Locale.forLanguageTag(lang)
                    : Locale.FRENCH;

            return new InviteConstraints(loc, isBlank(msg) ? null : msg);
        } catch (Exception e) {
            return InviteConstraints.DEFAULT;
        }
    }

    // ===== Helpers =====

    private static String trimTrailingSlash(String s) {
        if (s == null) {
            return "";
        }
        return s.endsWith("/") ? s.substring(0, s.length() - 1) : s;
    }

    private static boolean isBlank(@Nullable String s) {
        return s == null || s.trim().isEmpty();
    }

    private static String textOrNull(JsonNode n) {
        return (n == null || n.isNull()) ? null : n.asText(null);
    }

    private record InviteConstraints(Locale locale, @Nullable String message) {
        static final InviteConstraints DEFAULT = new InviteConstraints(Locale.FRENCH, null);
    }
}
