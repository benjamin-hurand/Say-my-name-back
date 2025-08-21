package com.saymyname.persistence.entity.course;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.math.BigDecimal;

import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.persistence.entity.GameModeEntity;
import com.saymyname.persistence.entity.PersonEntity;
import com.saymyname.persistence.entity.UserEntity;

@Entity
@Table(name = "knowledges")
public class KnowledgeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_mode_id", nullable = false)
    private GameModeEntity gameMode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private PersonEntity person;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 10, nullable = false)
    private KnowledgeStatus status;

    @Column(name = "next_review_date", nullable = false)
    private LocalDateTime nextReviewDate;

    @Column(name = "last_review_date")
    private LocalDateTime lastReviewDate;

    @Column(name = "total_repetition_count", nullable = false)
    private int totalRepetitionCount;

    @Column(name = "failure_count", nullable = false)
    private int failureCount;

    @Column(name = "success_count", nullable = false)
    private int successCount;

    @Column(name = "srs_streak", nullable = false)
    private int srsStreak;

    @Column(name = "global_streak", nullable = false)
    private int globalStreak;

    @Column(name = "ease_factor", nullable = false)
    private BigDecimal easeFactor;

    @Column(name = "difficulty", nullable = false)
    private double difficulty;

    @Column(name = "stability", nullable = false)
    private double stability;

    public KnowledgeEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public GameModeEntity getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameModeEntity gameMode) {
        this.gameMode = gameMode;
    }

    public PersonEntity getPerson() {
        return person;
    }

    public void setPerson(PersonEntity person) {
        this.person = person;
    }

    public KnowledgeStatus getStatus() {
        return status;
    }

    public void setStatus(KnowledgeStatus status) {
        this.status = status;
    }

    public LocalDateTime getNextReviewDate() {
        return nextReviewDate;
    }

    public void setNextReviewDate(LocalDateTime nextReviewDate) {
        this.nextReviewDate = nextReviewDate;
    }

    public LocalDateTime getLastReviewDate() {
        return lastReviewDate;
    }

    public void setLastReviewDate(LocalDateTime lastReviewDate) {
        this.lastReviewDate = lastReviewDate;
    }

    public int getTotalRepetitionCount() {
        return totalRepetitionCount;
    }

    public void setTotalRepetitionCount(int totalRepetitionCount) {
        this.totalRepetitionCount = totalRepetitionCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public void setSuccessCount(int successCount) {
        this.successCount = successCount;
    }

    public int getSrsStreak() {
        return srsStreak;
    }

    public void setSrsStreak(int srsStreak) {
        this.srsStreak = srsStreak;
    }

    public int getGlobalStreak() {
        return globalStreak;
    }

    public void setGlobalStreak(int globalStreak) {
        this.globalStreak = globalStreak;
    }

    public BigDecimal getEaseFactor() {
        return easeFactor;
    }

    public void setEaseFactor(BigDecimal easeFactor) {
        this.easeFactor = easeFactor;
    }

    public double getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(double difficulty) {
        this.difficulty = difficulty;
    }

    public double getStability() {
        return stability;
    }

    public void setStability(double stability) {
        this.stability = stability;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof KnowledgeEntity))
            return false;
        KnowledgeEntity that = (KnowledgeEntity) o;
        return id == that.id &&
                totalRepetitionCount == that.totalRepetitionCount &&
                failureCount == that.failureCount &&
                successCount == that.successCount &&
                srsStreak == that.srsStreak &&
                globalStreak == that.globalStreak &&
                Double.compare(that.difficulty, difficulty) == 0 &&
                Double.compare(that.stability, stability) == 0 &&
                Objects.equals(user, that.user) &&
                Objects.equals(gameMode, that.gameMode) &&
                Objects.equals(person, that.person) &&
                status == that.status &&
                Objects.equals(nextReviewDate, that.nextReviewDate) &&
                Objects.equals(lastReviewDate, that.lastReviewDate) &&
                Objects.equals(easeFactor, that.easeFactor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, gameMode, person, status,
                nextReviewDate, lastReviewDate,
                totalRepetitionCount, failureCount, successCount,
                srsStreak, globalStreak, easeFactor, difficulty, stability);
    }

    @Override
    public String toString() {
        return "KnowledgeEntity{" +
                "id=" + id +
                ", user=" + user +
                ", gameMode=" + gameMode +
                ", person=" + person +
                ", status=" + status +
                ", nextReviewDate=" + nextReviewDate +
                ", lastReviewDate=" + lastReviewDate +
                ", totalRepetitionCount=" + totalRepetitionCount +
                ", failureCount=" + failureCount +
                ", successCount=" + successCount +
                ", srsStreak=" + srsStreak +
                ", globalStreak=" + globalStreak +
                ", easeFactor=" + easeFactor +
                ", difficulty=" + difficulty +
                ", stability=" + stability +
                '}';
    }
}