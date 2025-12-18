// src/main/java/com/saymyname/service/port/Mailer.java
package com.saymyname.service.port;

import java.util.Locale;
import org.springframework.lang.Nullable;

import com.saymyname.core.model.enums.OrgRole;

public interface Mailer {

    void sendPasswordResetEmail(String to, String resetLink);

    void sendPasswordChangedInfoEmail(String to);

    // ➕ Nouveau : envoi d’une invitation
    void sendInvitationEmail(
            String to,
            String acceptUrl,
            @Nullable OrgRole role,
            Locale locale,
            @Nullable String message);
}
