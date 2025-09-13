package com.saymyname.infra.mail;

import jakarta.mail.internet.MimeMessage;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.saymyname.service.port.Mailer;

@Component
@Profile("!dev") // actif en prod, préprod, etc.
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
            // À toi de voir: logger/metrics; éventuellement rethrow custom
            throw new IllegalStateException("Unable to send email", e);
        }
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
}
