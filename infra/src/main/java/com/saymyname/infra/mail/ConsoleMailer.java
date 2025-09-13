package com.saymyname.infra.mail;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

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
}
