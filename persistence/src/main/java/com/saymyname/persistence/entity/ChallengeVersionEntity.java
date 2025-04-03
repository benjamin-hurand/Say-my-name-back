package com.saymyname.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "challenge_versions")
public class ChallengeVersionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

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

    public ChallengeVersionEntity() {}

    public ChallengeVersionEntity(long id, int versionNumber, LocalDateTime startDate, LocalDateTime endDate,
                                  ChallengeSeasonEntity firstSeason, ChallengeEntity challenge, int questionCount) {
        this.id = id;
        this.versionNumber = versionNumber;
        this.startDate = startDate;
        this.endDate = endDate;
        this.firstSeason = firstSeason;
        this.challenge = challenge;
        this.questionCount = questionCount;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public int getVersionNumber() { return versionNumber; }
    public void setVersionNumber(int versionNumber) { this.versionNumber = versionNumber; }
    public LocalDateTime getStartDate() { return startDate; }
    public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
    public LocalDateTime getEndDate() { return endDate; }
    public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
    public ChallengeSeasonEntity getFirstSeason() { return firstSeason; }
    public void setFirstSeason(ChallengeSeasonEntity firstSeason) { this.firstSeason = firstSeason; }
    public ChallengeEntity getChallenge() { return challenge; }
    public void setChallenge(ChallengeEntity challenge) { this.challenge = challenge; }
    public int getQuestionCount() { return questionCount; }
    public void setQuestionCount(int questionCount) { this.questionCount = questionCount; }

    public static class Builder {
        private long id;
        private int versionNumber;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private ChallengeSeasonEntity firstSeason;
        private ChallengeEntity challenge;
        private int questionCount;

        public Builder withId(long id) { this.id = id; return this; }
        public Builder withVersionNumber(int versionNumber) { this.versionNumber = versionNumber; return this; }
        public Builder withStartDate(LocalDateTime startDate) { this.startDate = startDate; return this; }
        public Builder withEndDate(LocalDateTime endDate) { this.endDate = endDate; return this; }
        public Builder withFirstSeason(ChallengeSeasonEntity firstSeason) { this.firstSeason = firstSeason; return this; }
        public Builder withChallenge(ChallengeEntity challenge) { this.challenge = challenge; return this; }
        public Builder withQuestionCount(int questionCount) { this.questionCount = questionCount; return this; }
        public ChallengeVersionEntity build() {
            return new ChallengeVersionEntity(id, versionNumber, startDate, endDate, firstSeason, challenge, questionCount);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChallengeVersionEntity that = (ChallengeVersionEntity) o;
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
        return "ChallengeVersionEntity{" +
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
