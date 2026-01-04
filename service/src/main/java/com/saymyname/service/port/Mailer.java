// src/main/java/com/saymyname/service/port/Mailer.java
package com.saymyname.service.port;

import java.util.Locale;
import org.springframework.lang.Nullable;

import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.model.enums.EmailVerificationPurpose;

public interface Mailer {

    void sendPasswordResetEmail(String to, String resetLink);

    void sendPasswordChangedInfoEmail(String to);

    void sendInvitationEmail(
            String to,
            String acceptUrl,
            @Nullable OrgRole role,
            Locale locale,
            @Nullable String message);

    void sendEmailVerificationOtp(
            String to,
            String code,
            EmailVerificationPurpose purpose,
            @Nullable String verificationLink,
            Locale locale);
}
