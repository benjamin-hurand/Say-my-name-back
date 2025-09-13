package com.saymyname.service.port;

public interface Mailer {
    void sendPasswordResetEmail(String to, String resetLink);

    void sendPasswordChangedInfoEmail(String to);
}
