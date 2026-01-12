package com.saymyname.infra.mail;

import com.saymyname.core.model.enums.EmailVerificationPurpose;
import com.saymyname.core.model.enums.OrgRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Unit tests for ConsoleMailer to ensure it handles email sending without throwing exceptions.
 * Since ConsoleMailer only logs to console, we verify it doesn't fail during execution.
 */
class ConsoleMailerTest {

    private ConsoleMailer consoleMailer;

    @BeforeEach
    void setUp() {
        consoleMailer = new ConsoleMailer();
    }

    @Test
    void sendPasswordResetEmail_doesNotThrow() {
        assertDoesNotThrow(() ->
                consoleMailer.sendPasswordResetEmail("test@example.com", "https://example.com/reset?token=abc"));
    }

    @Test
    void sendPasswordChangedInfoEmail_doesNotThrow() {
        assertDoesNotThrow(() ->
                consoleMailer.sendPasswordChangedInfoEmail("test@example.com"));
    }

    @Test
    void sendInvitationEmail_doesNotThrow() {
        assertDoesNotThrow(() ->
                consoleMailer.sendInvitationEmail(
                        "newuser@example.com",
                        "https://example.com/accept?token=xyz",
                        OrgRole.EDITOR,
                        Locale.FRENCH,
                        "Welcome to the team!"));
    }

    @Test
    void sendInvitationEmail_withNullRoleAndMessage_doesNotThrow() {
        assertDoesNotThrow(() ->
                consoleMailer.sendInvitationEmail(
                        "newuser@example.com",
                        "https://example.com/accept?token=xyz",
                        null,
                        Locale.ENGLISH,
                        null));
    }

    @Test
    void sendEmailVerificationOtp_doesNotThrow() {
        assertDoesNotThrow(() ->
                consoleMailer.sendEmailVerificationOtp(
                        "verify@example.com",
                        "123456",
                        EmailVerificationPurpose.REGISTER_EMAIL,
                        "https://example.com/verify?code=123456",
                        Locale.FRENCH));
    }

    @Test
    void sendEmailVerificationOtp_withNullLink_doesNotThrow() {
        assertDoesNotThrow(() ->
                consoleMailer.sendEmailVerificationOtp(
                        "verify@example.com",
                        "789012",
                        EmailVerificationPurpose.ADD_EMAIL,
                        null,
                        Locale.ENGLISH));
    }
}
