// src/main/java/com/saymyname/infra/mail/MailProperties.java
package com.saymyname.infra.mail;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.mail")
public class MailProperties {
    /** Adresse expéditrice (ex: "no-reply@tondomaine.com"). */
    private String from;
    /** Nom de marque (ex: "Say My Name"). */
    private String brand = "Say My Name";
    /** Sujets existants */
    private String resetSubject = "Réinitialisation de votre mot de passe";
    private String changedSubject = "Votre mot de passe a été modifié";
    /** ➕ Sujet pour les invitations */
    private String inviteSubject = "Votre invitation";

    // getters/setters
    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getResetSubject() {
        return resetSubject;
    }

    public void setResetSubject(String resetSubject) {
        this.resetSubject = resetSubject;
    }

    public String getChangedSubject() {
        return changedSubject;
    }

    public void setChangedSubject(String changedSubject) {
        this.changedSubject = changedSubject;
    }

    public String getInviteSubject() {
        return inviteSubject;
    }

    public void setInviteSubject(String inviteSubject) {
        this.inviteSubject = inviteSubject;
    }
}
