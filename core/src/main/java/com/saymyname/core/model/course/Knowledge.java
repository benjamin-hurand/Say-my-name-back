// src/main/java/com/saymyname/core/model/course/Knowledge.java
package com.saymyname.core.model.course;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.people.Fact;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Knowledge = état d'apprentissage SRS sur un Fact atomique.
 *
 * NOTE:
 * - Source de vérité: factId (FK).
 * - personId / attributeId supprimés : ils sont dérivables via Fact si besoin.
 */
public class Knowledge {

    private Long id;
    private User user;

    /** FK fonctionnelle (hot path). */
    private Long factId;

    /**
     * Optionnel: parfois chargé pour l'affichage (sinon fetch via service/dao).
     * Non persisté directement par Knowledge en DB.
     */
    private Fact fact;

    private KnowledgeStatus status;
    private LocalDateTime nextReviewDate;
    private LocalDateTime lastReviewDate;

    private int totalRepetitionCount;
    private int failureCount;
    private int successCount;
    private int srsStreak;
    private int globalStreak;

    private BigDecimal easeFactor;
    private double difficulty;
    private double stability;

    private boolean pendingRevalidation;
    private String revalidationReason;

    public Knowledge() {
    }

    private Knowledge(Builder b) {
        this.id = b.id;
        this.user = b.user;
        this.factId = b.factId;
        this.fact = b.fact;
        this.status = b.status;
        this.nextReviewDate = b.nextReviewDate;
        this.lastReviewDate = b.lastReviewDate;
        this.totalRepetitionCount = b.totalRepetitionCount;
        this.failureCount = b.failureCount;
        this.successCount = b.successCount;
        this.srsStreak = b.srsStreak;
        this.globalStreak = b.globalStreak;
        this.easeFactor = b.easeFactor;
        this.difficulty = b.difficulty;
        this.stability = b.stability;
        this.pendingRevalidation = b.pendingRevalidation;
        this.revalidationReason = b.revalidationReason;
    }

    // ---- getters/setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getFactId() {
        return factId;
    }

    public void setFactId(Long factId) {
        this.factId = factId;
    }

    public Fact getFact() {
        return fact;
    }

    public void setFact(Fact fact) {
        this.fact = fact;
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

    public boolean isPendingRevalidation() {
        return pendingRevalidation;
    }

    public void setPendingRevalidation(boolean pendingRevalidation) {
        this.pendingRevalidation = pendingRevalidation;
    }

    public String getRevalidationReason() {
        return revalidationReason;
    }

    public void setRevalidationReason(String revalidationReason) {
        this.revalidationReason = revalidationReason;
    }

    // ---- Builder

    public static class Builder {
        private Long id;
        private User user;
        private Long factId;
        private Fact fact;

        private KnowledgeStatus status;
        private LocalDateTime nextReviewDate;
        private LocalDateTime lastReviewDate;

        private int totalRepetitionCount;
        private int failureCount;
        private int successCount;
        private int srsStreak;
        private int globalStreak;

        private BigDecimal easeFactor;
        private double difficulty;
        private double stability;

        private boolean pendingRevalidation;
        private String revalidationReason;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withUser(User user) {
            this.user = user;
            return this;
        }

        public Builder withFactId(Long factId) {
            this.factId = factId;
            return this;
        }

        public Builder withFact(Fact fact) {
            this.fact = fact;
            return this;
        }

        public Builder withStatus(KnowledgeStatus status) {
            this.status = status;
            return this;
        }

        public Builder withNextReviewDate(LocalDateTime nextReviewDate) {
            this.nextReviewDate = nextReviewDate;
            return this;
        }

        public Builder withLastReviewDate(LocalDateTime lastReviewDate) {
            this.lastReviewDate = lastReviewDate;
            return this;
        }

        public Builder withTotalRepetitionCount(int totalRepetitionCount) {
            this.totalRepetitionCount = totalRepetitionCount;
            return this;
        }

        public Builder withFailureCount(int failureCount) {
            this.failureCount = failureCount;
            return this;
        }

        public Builder withSuccessCount(int successCount) {
            this.successCount = successCount;
            return this;
        }

        public Builder withSrsStreak(int srsStreak) {
            this.srsStreak = srsStreak;
            return this;
        }

        public Builder withGlobalStreak(int globalStreak) {
            this.globalStreak = globalStreak;
            return this;
        }

        public Builder withEaseFactor(BigDecimal easeFactor) {
            this.easeFactor = easeFactor;
            return this;
        }

        public Builder withDifficulty(double difficulty) {
            this.difficulty = difficulty;
            return this;
        }

        public Builder withStability(double stability) {
            this.stability = stability;
            return this;
        }

        public Builder withPendingRevalidation(boolean pendingRevalidation) {
            this.pendingRevalidation = pendingRevalidation;
            return this;
        }

        public Builder withRevalidationReason(String revalidationReason) {
            this.revalidationReason = revalidationReason;
            return this;
        }

        public Knowledge build() {
            return new Knowledge(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Knowledge))
            return false;
        Knowledge that = (Knowledge) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return (id != null) ? Objects.hash(id) : 0;
    }

    @Override
    public String toString() {
        return "Knowledge{" +
                "id=" + id +
                ", user=" + (user != null ? user.getId() : null) +
                ", factId=" + factId +
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
                ", pendingRevalidation=" + pendingRevalidation +
                ", revalidationReason=" + revalidationReason +
                '}';
    }
}