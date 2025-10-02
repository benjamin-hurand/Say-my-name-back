package com.saymyname.persistence.entity.organization;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.saymyname.persistence.multitenancy.BaseOrgScoped;

@Entity
@Table(name = "challenge_versions")
public class ChallengeVersionEntity extends BaseOrgScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "version_number", nullable = false)
    private int versionNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "first_season_id", nullable = false)
    private ChallengeSeasonEntity firstSeason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private ChallengeEntity challenge;

    @Column(name = "question_count", nullable = false)
    private int questionCount;

    // Nouveau champ : liste des questions associées à cette version
    @OneToMany(mappedBy = "version", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ChallengeQuestionEntity> questions = new ArrayList<>();

    public ChallengeVersionEntity() {
    }

    public ChallengeVersionEntity(Long id, int versionNumber, LocalDateTime startDate, LocalDateTime endDate,
            ChallengeSeasonEntity firstSeason, ChallengeEntity challenge, int questionCount,
            List<ChallengeQuestionEntity> questions) {
        this.id = id;
        this.versionNumber = versionNumber;
        this.startDate = startDate;
        this.endDate = endDate;
        this.firstSeason = firstSeason;
        this.challenge = challenge;
        this.questionCount = questionCount;
        this.questions = questions;
    }

    // Getters et setters existants
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public ChallengeSeasonEntity getFirstSeason() {
        return firstSeason;
    }

    public void setFirstSeason(ChallengeSeasonEntity firstSeason) {
        this.firstSeason = firstSeason;
    }

    public ChallengeEntity getChallenge() {
        return challenge;
    }

    public void setChallenge(ChallengeEntity challenge) {
        this.challenge = challenge;
    }

    public int getQuestionCount() {
        return questionCount;
    }

    public void setQuestionCount(int questionCount) {
        this.questionCount = questionCount;
    }

    public List<ChallengeQuestionEntity> getQuestions() {
        return questions;
    }

    public void setQuestions(List<ChallengeQuestionEntity> questions) {
        this.questions = questions;
    }

    // Builder pattern mis à jour avec le champ questions
    public static class Builder {
        private Long id;
        private int versionNumber;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private ChallengeSeasonEntity firstSeason;
        private ChallengeEntity challenge;
        private int questionCount;
        private List<ChallengeQuestionEntity> questions;

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

        public Builder withFirstSeason(ChallengeSeasonEntity firstSeason) {
            this.firstSeason = firstSeason;
            return this;
        }

        public Builder withChallenge(ChallengeEntity challenge) {
            this.challenge = challenge;
            return this;
        }

        public Builder withQuestionCount(int questionCount) {
            this.questionCount = questionCount;
            return this;
        }

        public Builder withQuestions(List<ChallengeQuestionEntity> questions) {
            this.questions = questions;
            return this;
        }

        public ChallengeVersionEntity build() {
            return new ChallengeVersionEntity(id, versionNumber, startDate, endDate, firstSeason, challenge,
                    questionCount, questions);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ChallengeVersionEntity))
            return false;
        ChallengeVersionEntity that = (ChallengeVersionEntity) o;
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
        return "ChallengeVersionEntity{" +
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
