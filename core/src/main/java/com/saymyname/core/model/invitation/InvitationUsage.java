package com.saymyname.core.model.invitation;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.people.Person;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Modèle "plat" côté core.
 * - Pas de référence forte vers Invitation pour éviter les cycles (on garde
 * invitationId).
 * - equals/hashCode basés uniquement sur id.
 */
public class InvitationUsage {

    private Long id;

    private Long invitationId; // évite la boucle Invitation -> usages -> Invitation
    private User user; // qui a consommé
    private Person person; // fiche nominative associée, optionnel

    private LocalDateTime usedAt;
    private byte[] usedIp; // IPv4/IPv6 compact
    private String userAgent;

    public InvitationUsage() {
    }

    private InvitationUsage(Builder b) {
        this.id = b.id;
        this.invitationId = b.invitationId;
        this.user = b.user;
        this.person = b.person;
        this.usedAt = b.usedAt;
        this.usedIp = b.usedIp;
        this.userAgent = b.userAgent;
    }

    // --- Getters
    public Long getId() {
        return id;
    }

    public Long getInvitationId() {
        return invitationId;
    }

    public User getUser() {
        return user;
    }

    public Person getPerson() {
        return person;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public byte[] getUsedIp() {
        return usedIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    // --- Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setInvitationId(Long invitationId) {
        this.invitationId = invitationId;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }

    public void setUsedIp(byte[] usedIp) {
        this.usedIp = usedIp;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    // --- Builder
    public static class Builder {
        private Long id;
        private Long invitationId;
        private User user;
        private Person person;
        private LocalDateTime usedAt;
        private byte[] usedIp;
        private String userAgent;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withInvitationId(Long invitationId) {
            this.invitationId = invitationId;
            return this;
        }

        public Builder withUser(User user) {
            this.user = user;
            return this;
        }

        public Builder withPerson(Person person) {
            this.person = person;
            return this;
        }

        public Builder withUsedAt(LocalDateTime usedAt) {
            this.usedAt = usedAt;
            return this;
        }

        public Builder withUsedIp(byte[] usedIp) {
            this.usedIp = usedIp;
            return this;
        }

        public Builder withUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public InvitationUsage build() {
            return new InvitationUsage(this);
        }
    }

    // --- equals/hashCode par id uniquement
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof InvitationUsage))
            return false;
        InvitationUsage that = (InvitationUsage) o;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return "InvitationUsage{" +
                "id=" + id +
                ", invitationId=" + invitationId +
                ", userId=" + (user != null ? user.getId() : null) +
                ", personId=" + (person != null ? person.getId() : null) +
                ", usedAt=" + usedAt +
                ", ua='" + userAgent + '\'' +
                '}';
    }
}
