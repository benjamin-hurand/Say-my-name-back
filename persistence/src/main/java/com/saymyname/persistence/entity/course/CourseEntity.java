// src/main/java/com/saymyname/persistence/entity/course/CourseEntity.java
package com.saymyname.persistence.entity.course;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

import com.saymyname.core.model.enums.CourseStatus;
import com.saymyname.core.model.enums.PopulationScope;
import com.saymyname.persistence.entity.GameModeEntity;
import com.saymyname.persistence.entity.UserEntity;

@Entity
@Table(name = "courses")
public class CourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // user_id -> users.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    // game_mode_id -> game_modes.id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_mode_id", nullable = false)
    private GameModeEntity gameMode;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 12)
    private CourseStatus status;

    @Column(name = "current_round", nullable = false)
    private int currentRound = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "population_scope", nullable = false, length = 32)
    private PopulationScope populationScope = PopulationScope.FOLLOWED;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    // Nouveau : accès utilisateur
    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    public CourseEntity() {
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

    public CourseStatus getStatus() {
        return status;
    }

    public void setStatus(CourseStatus status) {
        this.status = status;
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(int currentRound) {
        this.currentRound = currentRound;
    }

    public PopulationScope getPopulationScope() {
        return populationScope;
    }

    public void setPopulationScope(PopulationScope populationScope) {
        this.populationScope = populationScope;
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

    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }

    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof CourseEntity))
            return false;
        CourseEntity that = (CourseEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CourseEntity{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", gameModeId=" + (gameMode != null ? gameMode.getId() : null) +
                ", status=" + status +
                ", currentRound=" + currentRound +
                ", populationScope=" + populationScope +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", lastAccessedAt=" + lastAccessedAt +
                '}';
    }
}
