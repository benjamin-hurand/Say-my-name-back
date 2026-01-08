package com.saymyname.persistence.entity.organization.course;

import java.time.LocalDateTime;
import java.util.Objects;

import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.GameModeEntity;
import com.saymyname.persistence.entity.organization.PersonEntity;
import com.saymyname.persistence.multitenancy.BaseOrgScoped;

import jakarta.persistence.*;

@Entity
@Table(name = "knowledge_stats", uniqueConstraints = {
        @UniqueConstraint(name = "uq_ks", columnNames = { "organization_id", "user_id", "game_mode_id",
                "knowledge_id" })
}, indexes = {
        @Index(name = "idx_ks_select", columnList = "organization_id, user_id, game_mode_id, error_streak, avg_rt_recent, last_answer_at"),
        @Index(name = "idx_ks_person", columnList = "organization_id, user_id, game_mode_id, person_id")
})
public class KnowledgeStatsEntity extends BaseOrgScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_mode_id", nullable = false)
    private GameModeEntity gameMode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "knowledge_id", nullable = false)
    private KnowledgeEntity knowledge;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_id", nullable = false)
    private PersonEntity person;

    @Column(name = "attempts_recent", nullable = false)
    private double attemptsRecent;

    @Column(name = "correct_recent", nullable = false)
    private double correctRecent;

    @Column(name = "help_recent", nullable = false)
    private double helpRecent;

    @Column(name = "avg_rt_recent", nullable = false)
    private double avgRtRecent;

    @Column(name = "last_answer_at")
    private LocalDateTime lastAnswerAt;

    @Column(name = "last_correct")
    private Boolean lastCorrect;

    @Column(name = "last_help_used")
    private Boolean lastHelpUsed;

    @Column(name = "last_response_time_ms", nullable = false)
    private int lastResponseTimeMs;

    @Column(name = "error_streak", nullable = false)
    private int errorStreak;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public KnowledgeStatsEntity() {
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

    public KnowledgeEntity getKnowledge() {
        return knowledge;
    }

    public void setKnowledge(KnowledgeEntity knowledge) {
        this.knowledge = knowledge;
    }

    public PersonEntity getPerson() {
        return person;
    }

    public void setPerson(PersonEntity person) {
        this.person = person;
    }

    public double getAttemptsRecent() {
        return attemptsRecent;
    }

    public void setAttemptsRecent(double attemptsRecent) {
        this.attemptsRecent = attemptsRecent;
    }

    public double getCorrectRecent() {
        return correctRecent;
    }

    public void setCorrectRecent(double correctRecent) {
        this.correctRecent = correctRecent;
    }

    public double getHelpRecent() {
        return helpRecent;
    }

    public void setHelpRecent(double helpRecent) {
        this.helpRecent = helpRecent;
    }

    public double getAvgRtRecent() {
        return avgRtRecent;
    }

    public void setAvgRtRecent(double avgRtRecent) {
        this.avgRtRecent = avgRtRecent;
    }

    public LocalDateTime getLastAnswerAt() {
        return lastAnswerAt;
    }

    public void setLastAnswerAt(LocalDateTime lastAnswerAt) {
        this.lastAnswerAt = lastAnswerAt;
    }

    public Boolean getLastCorrect() {
        return lastCorrect;
    }

    public void setLastCorrect(Boolean lastCorrect) {
        this.lastCorrect = lastCorrect;
    }

    public Boolean getLastHelpUsed() {
        return lastHelpUsed;
    }

    public void setLastHelpUsed(Boolean lastHelpUsed) {
        this.lastHelpUsed = lastHelpUsed;
    }

    public int getLastResponseTimeMs() {
        return lastResponseTimeMs;
    }

    public void setLastResponseTimeMs(int lastResponseTimeMs) {
        this.lastResponseTimeMs = lastResponseTimeMs;
    }

    public int getErrorStreak() {
        return errorStreak;
    }

    public void setErrorStreak(int errorStreak) {
        this.errorStreak = errorStreak;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof KnowledgeStatsEntity))
            return false;
        KnowledgeStatsEntity that = (KnowledgeStatsEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "KnowledgeStatsEntity{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", gameModeId=" + (gameMode != null ? gameMode.getId() : null) +
                ", knowledgeId=" + (knowledge != null ? knowledge.getId() : null) +
                ", personId=" + (person != null ? person.getId() : null) +
                ", attemptsRecent=" + attemptsRecent +
                ", correctRecent=" + correctRecent +
                ", helpRecent=" + helpRecent +
                ", avgRtRecent=" + avgRtRecent +
                ", lastAnswerAt=" + lastAnswerAt +
                ", lastCorrect=" + lastCorrect +
                ", lastHelpUsed=" + lastHelpUsed +
                ", lastResponseTimeMs=" + lastResponseTimeMs +
                ", errorStreak=" + errorStreak +
                '}';
    }
}
