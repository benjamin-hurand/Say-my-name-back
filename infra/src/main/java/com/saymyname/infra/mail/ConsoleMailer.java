// src/main/java/com/saymyname/infra/mail/ConsoleMailer.java
package com.saymyname.infra.mail;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.EmailVerificationPurpose;
import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.service.port.Mailer;

@Component
@Profile("dev")
public class ConsoleMailer implements Mailer {

    private final Logger logger = LoggerFactory.getLogger(ConsoleMailer.class);

    @Override
    public void sendPasswordResetEmail(String to, String resetLink) {
        logger.info("[DEV MAIL] To={} | ResetLink={}", to, resetLink);
    }

    @Override
    public void sendPasswordChangedInfoEmail(String to) {
        logger.info("[DEV MAIL] To={} | Password changed.", to);
    }

    @Override
    public void sendInvitationEmail(String to, String acceptUrl, @Nullable OrgRole role, Locale locale,
            @Nullable String message) {
        logger.info("[DEV MAIL] Invitation | To={} | Url={} | Role={} | Locale={} | Message={}",
                to, acceptUrl, role, locale, message);
    }

    @Override
    public void sendEmailVerificationOtp(String to, String code, EmailVerificationPurpose purpose,
            @Nullable String verificationLink, Locale locale) {
        logger.info("[DEV MAIL] EmailVerification | To={} | Purpose={} | Locale={} | OTP={} | Link={}",
                to, purpose, locale, code, verificationLink);
    }
}
