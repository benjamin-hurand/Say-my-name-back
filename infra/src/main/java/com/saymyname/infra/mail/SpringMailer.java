// src/main/java/com/saymyname/infra/mail/SpringMailer.java
package com.saymyname.infra.mail;

import jakarta.mail.internet.MimeMessage;

import java.util.Locale;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.lang.Nullable;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.EmailVerificationPurpose;
import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.service.port.Mailer;

@Component
@Profile("prod")
@EnableConfigurationProperties(MailProperties.class)
public class SpringMailer implements Mailer {

  private final JavaMailSender mailSender;
  private final MailProperties props;

  public SpringMailer(JavaMailSender mailSender, MailProperties props) {
    this.mailSender = mailSender;
    this.props = props;
  }

  @Override
  public void sendPasswordResetEmail(String to, String resetLink) {
    sendHtml(to, props.getResetSubject(), buildResetHtml(props.getBrand(), resetLink));
  }

  @Override
  public void sendPasswordChangedInfoEmail(String to) {
    sendHtml(to, props.getChangedSubject(), buildChangedHtml(props.getBrand()));
  }

  @Override
  public void sendInvitationEmail(String to, String acceptUrl, @Nullable OrgRole role, Locale locale,
      @Nullable String message) {
    String subject = props.getInviteSubject();
    String html = buildInviteHtml(props.getBrand(), acceptUrl, role, message);
    sendHtml(to, subject, html);
  }

  @Override
  public void sendEmailVerificationOtp(String to, String code, EmailVerificationPurpose purpose,
      @Nullable String verificationLink, Locale locale) {
    // Tu peux i18n plus tard via locale
    String subject = props.getBrand() + " — Vérification email";
    String html = buildEmailVerificationHtml(props.getBrand(), code, purpose, verificationLink);
    sendHtml(to, subject, html);
  }

  private void sendHtml(String to, String subject, String html) {
    try {
      MimeMessage msg = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(msg, "UTF-8");
      helper.setFrom(props.getFrom());
      helper.setTo(to);
      helper.setSubject(subject);
      helper.setText(html, true);
      mailSender.send(msg);
    } catch (Exception e) {
      throw new IllegalStateException("Unable to send email", e);
    }
  }

  private static String buildEmailVerificationHtml(String brand, String code, EmailVerificationPurpose purpose,
      @Nullable String link) {

    String purposeLine = (purpose == null) ? "" : "<p>Type : <strong>" + escape(purpose.name()) + "</strong></p>";
    String linkLine = (link == null || link.isBlank())
        ? ""
        : """
              <p>Lien direct (optionnel) :</p>
              <p><a href="%s">%s</a></p>
            """.formatted(escape(link), escape(link));

    return """
          <div style="font-family:Arial,sans-serif;line-height:1.5">
            <h2>%s — Vérification d'email</h2>
            %s
            <p>Voici votre code de vérification (valable quelques minutes) :</p>
            <p style="font-size:24px;letter-spacing:2px"><strong>%s</strong></p>
            %s
            <p>Si vous n'êtes pas à l'origine de cette demande, ignorez ce message.</p>
            <hr/>
            <small>© %s</small>
          </div>
        """.formatted(escape(brand), purposeLine, escape(code), linkLine, escape(brand));
  }

  private static String buildResetHtml(String brand, String link) {
    return """
            <div style="font-family:Arial,sans-serif;line-height:1.5">
              <h2>%s — Réinitialisation du mot de passe</h2>
              <p>Vous avez demandé la réinitialisation de votre mot de passe.</p>
              <p>Veuillez cliquer sur le bouton ci-dessous (lien valable ~30 minutes) :</p>
              <p>
                <a href="%s" style="background:#1f6feb;color:#fff;padding:10px 16px;text-decoration:none;border-radius:6px;">
                  Réinitialiser mon mot de passe
                </a>
              </p>
              <p>Si vous n'êtes pas à l'origine de cette demande, ignorez ce message.</p>
              <hr/>
              <small>© %s</small>
            </div>
        """
        .formatted(brand, link, brand);
  }

  private static String buildChangedHtml(String brand) {
    return """
            <div style="font-family:Arial,sans-serif;line-height:1.5">
              <h2>%s — Mot de passe modifié</h2>
              <p>Votre mot de passe a été modifié avec succès.</p>
              <p>Si vous n'êtes pas à l'origine de cette action, réinitialisez-le immédiatement.</p>
              <hr/>
              <small>© %s</small>
            </div>
        """.formatted(brand, brand);
  }

  private static String buildInviteHtml(String brand, String acceptUrl, @Nullable OrgRole role,
      @Nullable String message) {
    String roleLine = (role == null) ? ""
        : "<p>Rôle proposé : <strong>" + escape(role.toString()) + "</strong></p>";
    String msgLine = (message == null || message.isBlank()) ? ""
        : "<p style='white-space:pre-wrap'>" + escape(message) + "</p>";
    return """
         <div style="font-family:Arial,sans-serif;line-height:1.5">
           <h2>%s — Invitation</h2>
           %s
           %s
           <p>Veuillez cliquer sur le bouton ci-dessous pour accepter l'invitation :</p>
           <p>
             <a href="%s" style="background:#1f6feb;color:#fff;padding:10px 16px;text-decoration:none;border-radius:6px;">
               Accepter l'invitation
             </a>
           </p>
           <hr/>
           <small>© %s</small>
         </div>
        """
        .formatted(brand, roleLine, msgLine, acceptUrl, brand);
  }

  private static String escape(String s) {
    if (s == null)
      return "";
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
  }
}
