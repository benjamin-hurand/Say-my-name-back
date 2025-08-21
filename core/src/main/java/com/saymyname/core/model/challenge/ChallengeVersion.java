package com.saymyname.core.model.challenge;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Représente une version (snapshot) d'un challenge pour une période donnée.
 */
public class ChallengeVersion {
    private Long id;
    private int versionNumber;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private ChallengeSeason firstSeason; // Première saison à partir de laquelle la snapshot est active
    private Challenge challenge; // Le challenge auquel cette version appartient
    private int questionCount; // Nombre total de questions pour cette version

    // Nouveau champ : liste des questions associées
    private List<ChallengeQuestion> questions;

    // Constructeur par défaut
    public ChallengeVersion() {
    }

    private ChallengeVersion(Builder builder) {
        this.id = builder.id;
        this.versionNumber = builder.versionNumber;
        this.startDate = builder.startDate;
        this.endDate = builder.endDate;
        this.firstSeason = builder.firstSeason;
        this.challenge = builder.challenge;
        this.questionCount = builder.questionCount;
        this.questions = builder.questions;
    }

    // Getters
    public Long getId() {
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

    public List<ChallengeQuestion> getQuestions() {
        return questions;
    }

    // Setters
    public void setId(Long id) {
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

    public void setQuestions(List<ChallengeQuestion> questions) {
        this.questions = questions;
    }

    // Builder
    public static class Builder {
        private Long id;
        private int versionNumber;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private ChallengeSeason firstSeason;
        private Challenge challenge;
        private int questionCount;
        private List<ChallengeQuestion> questions;

        public Builder withId(Long id) {
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

        // Ajout de la méthode pour définir la liste des questions
        public Builder withQuestions(List<ChallengeQuestion> questions) {
            this.questions = questions;
            return this;
        }

        public ChallengeVersion build() {
            return new ChallengeVersion(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    // Les méthodes equals, hashCode et toString peuvent être mises à jour pour
    // inclure le champ "questions" si nécessaire.
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ChallengeVersion))
            return false;
        ChallengeVersion that = (ChallengeVersion) o;
        return id == that.id &&
                versionNumber == that.versionNumber &&
                questionCount == that.questionCount &&
                Objects.equals(startDate, that.startDate) &&
                Objects.equals(endDate, that.endDate) &&
                Objects.equals(firstSeason, that.firstSeason) &&
                Objects.equals(challenge, that.challenge) &&
                Objects.equals(questions, that.questions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, versionNumber, startDate, endDate, firstSeason, challenge, questionCount, questions);
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
                ", questions=" + questions +
                '}';
    }
}
