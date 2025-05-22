package com.saymyname.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

import com.saymyname.core.model.enums.AttemptStatus;

@Entity
@Table(name = "challenge_attempts")
public class ChallengeAttemptEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_version_id", nullable = false)
    private ChallengeVersionEntity challengeVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AttemptStatus status;

    @Column(name = "attempt_start", nullable = true, columnDefinition = "DATETIME(3)")
    private LocalDateTime attemptStart;

    @Column(name = "attempt_end", nullable = true, columnDefinition = "DATETIME(3)")
    private LocalDateTime attemptEnd;

    @Column(name = "correct_answers", nullable = false)
    private int correctAnswers;

    public ChallengeAttemptEntity() {
    }

    public ChallengeAttemptEntity(long id, UserEntity user, ChallengeVersionEntity challengeVersion,
            AttemptStatus status,
            LocalDateTime attemptStart, LocalDateTime attemptEnd, int correctAnswers) {
        this.id = id;
        this.user = user;
        this.challengeVersion = challengeVersion;
        this.status = status;
        this.attemptStart = attemptStart;
        this.attemptEnd = attemptEnd;
        this.correctAnswers = correctAnswers;
    }

    // Getters and setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public ChallengeVersionEntity getChallengeVersion() {
        return challengeVersion;
    }

    public void setChallengeVersion(ChallengeVersionEntity challengeVersion) {
        this.challengeVersion = challengeVersion;
    }

    public AttemptStatus getStatus() {
        return status;
    }

    public void setStatus(AttemptStatus status) {
        this.status = status;
    }

    public LocalDateTime getAttemptStart() {
        return attemptStart;
    }

    public void setAttemptStart(LocalDateTime attemptStart) {
        this.attemptStart = attemptStart;
    }

    public LocalDateTime getAttemptEnd() {
        return attemptEnd;
    }

    public void setAttemptEnd(LocalDateTime attemptEnd) {
        this.attemptEnd = attemptEnd;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    // Builder
    public static class Builder {
        private long id;
        private UserEntity user;
        private ChallengeVersionEntity challengeVersion;
        private AttemptStatus status;
        private LocalDateTime attemptStart;
        private LocalDateTime attemptEnd;
        private int correctAnswers;

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withUser(UserEntity user) {
            this.user = user;
            return this;
        }

        public Builder withChallengeVersion(ChallengeVersionEntity challengeVersion) {
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

        public ChallengeAttemptEntity build() {
            return new ChallengeAttemptEntity(id, user, challengeVersion, status, attemptStart, attemptEnd,
                    correctAnswers);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ChallengeAttemptEntity that = (ChallengeAttemptEntity) o;
        return id == that.id &&
                correctAnswers == that.correctAnswers &&
                Objects.equals(user, that.user) &&
                Objects.equals(challengeVersion, that.challengeVersion) &&
                status == that.status &&
                Objects.equals(attemptStart, that.attemptStart) &&
                Objects.equals(attemptEnd, that.attemptEnd);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, challengeVersion, status, attemptStart, attemptEnd, correctAnswers);
    }

    @Override
    public String toString() {
        return "ChallengeAttemptEntity{" +
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
