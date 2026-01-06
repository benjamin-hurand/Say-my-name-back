// src/main/java/com/saymyname/persistence/entity/organization/leaderboard/LeaderboardStatEntity.java
package com.saymyname.persistence.entity.organization.leaderboard;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.*;

import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.multitenancy.BaseOrgScoped;

@Entity
@Table(name = "leaderboard_stats", uniqueConstraints = {
        @UniqueConstraint(name = "uq_lb_org_user", columnNames = { "organization_id", "user_id" })
}, indexes = {
        @Index(name = "idx_lb_org_xp", columnList = "organization_id, xp"),
        @Index(name = "idx_lb_org_correct", columnList = "organization_id, correct_answers")
})
public class LeaderboardStatEntity extends BaseOrgScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    /** XP total cumulé */
    @Column(name = "xp", nullable = false)
    private long xp;

    /** total_answers */
    @Column(name = "total_answers", nullable = false)
    private long totalAnswers;

    /** correct_answers */
    @Column(name = "correct_answers", nullable = false)
    private long correctAnswers;

    /** last_answer_at (ex last_event_at) */
    @Column(name = "last_answer_at")
    private LocalDateTime lastAnswerAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public LeaderboardStatEntity() {
    }

    @PrePersist
    protected void onCreate() {
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ---------- Getters / Setters ----------

    public Long getId() {
        return id;
    }

    public void setId(Long id) { // utile si mapping/import
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public long getXp() {
        return xp;
    }

    public void setXp(long xp) {
        this.xp = xp;
    }

    public long getTotalAnswers() {
        return totalAnswers;
    }

    public void setTotalAnswers(long totalAnswers) {
        this.totalAnswers = totalAnswers;
    }

    public long getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(long correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public LocalDateTime getLastAnswerAt() {
        return lastAnswerAt;
    }

    public void setLastAnswerAt(LocalDateTime lastAnswerAt) {
        this.lastAnswerAt = lastAnswerAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) { // optionnel
        this.updatedAt = updatedAt;
    }

    // ---------- equals / hashCode / toString ----------

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof LeaderboardStatEntity))
            return false;
        LeaderboardStatEntity that = (LeaderboardStatEntity) o;
        return xp == that.xp
                && totalAnswers == that.totalAnswers
                && correctAnswers == that.correctAnswers
                && Objects.equals(id, that.id)
                && Objects.equals(user, that.user)
                && Objects.equals(lastAnswerAt, that.lastAnswerAt)
                && Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, xp, totalAnswers, correctAnswers, lastAnswerAt, updatedAt);
    }

    @Override
    public String toString() {
        return "LeaderboardStatEntity{" +
                "id=" + id +
                ", user=" + (user != null ? user.getId() : null) +
                ", xp=" + xp +
                ", totalAnswers=" + totalAnswers +
                ", correctAnswers=" + correctAnswers +
                ", lastAnswerAt=" + lastAnswerAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
