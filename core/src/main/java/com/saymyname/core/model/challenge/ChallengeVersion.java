package com.saymyname.core.model.challenge;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Représente une version (snapshot) d'un challenge pour une période donnée.
 */
public class ChallengeVersion {
    private long id;
    private int versionNumber;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private ChallengeSeason firstSeason; // Première saison à partir de laquelle la snapshot est active
    private Challenge challenge;         // Le challenge auquel cette version appartient
    private int questionCount;           // Nombre total de questions pour cette version

    // Constructeur par défaut
    public ChallengeVersion() {}

    private ChallengeVersion(Builder builder) {
        this.id = builder.id;
        this.versionNumber = builder.versionNumber;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.firstSeason = builder.firstSeason;
        this.challenge = builder.challenge;
        this.questionCount = builder.questionCount;
    }

    // Getters
    public long getId() {
        return id;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public ChallengeSeason getFirstSeason() {
        return firstSeason;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    // Setters
    public void setId(long id) {
        this.id = id;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public void setFirstSeason(ChallengeSeason firstSeason) {
        this.firstSeason = firstSeason;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    // Builder
    public static class Builder {
        private long id;
        private int versionNumber;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private ChallengeSeason firstSeason;
        private Challenge challenge;
        private int questionCount;

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withVersionNumber(int versionNumber) {
            this.versionNumber = versionNumber;
            return this;
        }

        public Builder withStartDate(LocalDateTime startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder withEndDate(LocalDateTime endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder withFirstSeason(ChallengeSeason firstSeason) {
            this.firstSeason = firstSeason;
            return this;
        }

        public Builder withChallenge(Challenge challenge) {
            this.challenge = challenge;
            return this;
        }

        public Builder withQuestionCount(int questionCount) {
            this.questionCount = questionCount;
            return this;
        }

        public ChallengeVersion build() {
            return new ChallengeVersion(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChallengeVersion that = (ChallengeVersion) o;
        return id == that.id &&
               versionNumber == that.versionNumber &&
               questionCount == that.questionCount &&
               Objects.equals(startDate, that.startDate) &&
               Objects.equals(endDate, that.endDate) &&
               Objects.equals(firstSeason, that.firstSeason) &&
               Objects.equals(challenge, that.challenge);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, versionNumber, startDate, endDate, firstSeason, challenge, questionCount);
    }

    @Override
    public String toString() {
        return "ChallengeVersion{" +
               "id=" + id +
               ", versionNumber=" + versionNumber +
               ", startDate=" + startDate +
               ", endDate=" + endDate +
               ", firstSeason=" + firstSeason +
               ", challenge=" + challenge +
               ", questionCount=" + questionCount +
               '}';
    }
}
