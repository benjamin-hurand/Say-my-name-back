package com.saymyname.core.model.challenge;

import com.saymyname.core.model.common.User;
import com.saymyname.core.model.enums.AttemptStatus;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Représente une tentative de participation à une version de challenge par un
 * utilisateur.
 */
public class ChallengeAttempt {
    private Long id;
    private User user; // Utilisateur participant
    private ChallengeVersion challengeVersion; // Version du challenge tentée
    private AttemptStatus status; // Statut de la tentative (en cours, terminée, abandonnée)
    private LocalDateTime attemptStart; // Heure de début (DATETIME(3))
    private LocalDateTime attemptEnd; // Heure de fin (DATETIME(3))
    private int correctAnswers; // Nombre de réponses correctes obtenues

    // Constructeur par défaut
    public ChallengeAttempt() {
    }

    private ChallengeAttempt(Builder builder) {
        this.id = builder.id;
        this.user = builder.user;
        this.challengeVersion = builder.challengeVersion;
        this.status = builder.status;
        this.attemptStart = builder.attemptStart;
        this.attemptEnd = builder.attemptEnd;
        this.correctAnswers = builder.correctAnswers;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public ChallengeVersion getChallengeVersion() {
        return challengeVersion;
    }

    public AttemptStatus getStatus() {
        return status;
    }

    public LocalDateTime getAttemptStart() {
        return attemptStart;
    }

    public LocalDateTime getAttemptEnd() {
        return attemptEnd;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setChallengeVersion(ChallengeVersion challengeVersion) {
        this.challengeVersion = challengeVersion;
    }

    public void setStatus(AttemptStatus status) {
        this.status = status;
    }

    public void setAttemptStart(LocalDateTime attemptStart) {
        this.attemptStart = attemptStart;
    }

    public void setAttemptEnd(LocalDateTime attemptEnd) {
        this.attemptEnd = attemptEnd;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    // Builder
    public static class Builder {
        private Long id;
        private User user;
        private ChallengeVersion challengeVersion;
        private AttemptStatus status;
        private LocalDateTime attemptStart;
        private LocalDateTime attemptEnd;
        private int correctAnswers;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withUser(User user) {
            this.user = user;
            return this;
        }

        public Builder withChallengeVersion(ChallengeVersion challengeVersion) {
            this.challengeVersion = challengeVersion;
            return this;
        }

        public Builder withStatus(AttemptStatus status) {
            this.status = status;
            return this;
        }

        public Builder withAttemptStart(LocalDateTime attemptStart) {
            this.attemptStart = attemptStart;
            return this;
        }

        public Builder withAttemptEnd(LocalDateTime attemptEnd) {
            this.attemptEnd = attemptEnd;
            return this;
        }

        public Builder withCorrectAnswers(int correctAnswers) {
            this.correctAnswers = correctAnswers;
            return this;
        }

        public ChallengeAttempt build() {
            return new ChallengeAttempt(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ChallengeAttempt that = (ChallengeAttempt) o;
        return id == that.id &&
                correctAnswers == that.correctAnswers &&
                Objects.equals(user, that.user) &&
                Objects.equals(challengeVersion, that.challengeVersion) &&
                Objects.equals(status, that.status) &&
                Objects.equals(attemptStart, that.attemptStart) &&
                Objects.equals(attemptEnd, that.attemptEnd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, challengeVersion, status, attemptStart, attemptEnd, correctAnswers);
    }

    @Override
    public String toString() {
        return "ChallengeAttempt{" +
                "id=" + id +
                ", user=" + user +
                ", challengeVersion=" + challengeVersion +
                ", status=" + status +
                ", attemptStart=" + attemptStart +
                ", attemptEnd=" + attemptEnd +
                ", correctAnswers=" + correctAnswers +
                '}';
    }
}
