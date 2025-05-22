package com.saymyname.persistence.entity.course;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import com.saymyname.core.model.enums.CourseStatus;
import com.saymyname.persistence.entity.AttributeEntity;
import com.saymyname.persistence.entity.GameModeEntity;
import com.saymyname.persistence.entity.UserEntity;

@Entity
@Table(name = "courses")
public class CourseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_mode_id", nullable = false)
    private GameModeEntity gameMode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sorting_attribute_id")
    private AttributeEntity sortingAttribute;

    @Column(name = "sorting_order", nullable = false)
    private String sortingOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 12)
    private CourseStatus status;

    @Column(name = "current_round", nullable = false)
    private int currentRound = 0;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "course_populations", joinColumns = @JoinColumn(name = "course_id"), inverseJoinColumns = @JoinColumn(name = "population_id"))
    private List<PopulationEntity> populations;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    public CourseEntity() {
    }

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

    public GameModeEntity getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameModeEntity gameMode) {
        this.gameMode = gameMode;
    }

    public AttributeEntity getSortingAttribute() {
        return sortingAttribute;
    }

    public void setSortingAttribute(AttributeEntity sortingAttribute) {
        this.sortingAttribute = sortingAttribute;
    }

    public String getSortingOrder() {
        return sortingOrder;
    }

    public void setSortingOrder(String sortingOrder) {
        this.sortingOrder = sortingOrder;
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

    public List<PopulationEntity> getPopulations() {
        return populations;
    }

    public void setPopulations(List<PopulationEntity> populations) {
        this.populations = populations;
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
        if (!(o instanceof CourseEntity))
            return false;
        CourseEntity c = (CourseEntity) o;
        return id == c.id &&
                Objects.equals(user, c.user) &&
                Objects.equals(gameMode, c.gameMode) &&
                Objects.equals(sortingAttribute, c.sortingAttribute) &&
                Objects.equals(sortingOrder, c.sortingOrder) &&
                Objects.equals(status, c.status) &&
                Objects.equals(populations, c.populations);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, user, gameMode,
                sortingAttribute, sortingOrder,
                status, populations);
    }

    @Override
    public String toString() {
        return "CourseEntity{" +
                "id=" + id +
                ", user=" + user +
                ", gameMode=" + gameMode +
                ", sortingAttribute=" + sortingAttribute +
                ", sortingOrder='" + sortingOrder + '\'' +
                ", status='" + status + '\'' +
                ", populations=" + populations +
                '}';
    }
}
