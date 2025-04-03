package com.saymyname.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "challenges")
public class ChallengeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    // Association vers la table game_modes
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mode_id", nullable = false)
    private GameModeEntity gameMode;

    // Association vers la table attributes pour le filtre
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filter_id", nullable = false)
    private AttributeEntity filterAttribute;

    @Column(name = "min_filter_value")
    private String minFilterValue;

    @Column(name = "max_filter_value")
    private String maxFilterValue;

    @Column(name = "date_creation", nullable = true, columnDefinition = "DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3)")
    private LocalDateTime creationDate;

    // Association vers la table users (créateur du challenge)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private UserEntity creator;

    // Relation bidirectionnelle avec ChallengeVersionEntity
    @OneToMany(mappedBy = "challenge", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengeVersionEntity> challengeVersions = new ArrayList<>();

    public ChallengeEntity() {}

    private ChallengeEntity(Builder builder) {
        this.id = builder.id;
        this.description = builder.description;
        this.gameMode = builder.gameMode;
        this.filterAttribute = builder.filterAttribute;
        this.minFilterValue = builder.minFilterValue;
        this.maxFilterValue = builder.maxFilterValue;
        this.creationDate = builder.creationDate;
        this.creator = builder.creator;
        this.challengeVersions = builder.challengeVersions;
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public GameModeEntity getGameMode() { return gameMode; }
    public void setGameMode(GameModeEntity gameMode) { this.gameMode = gameMode; }
    public AttributeEntity getFilterAttribute() { return filterAttribute; }
    public void setFilterAttribute(AttributeEntity filterAttribute) { this.filterAttribute = filterAttribute; }
    public String getMinFilterValue() { return minFilterValue; }
    public void setMinFilterValue(String minFilterValue) { this.minFilterValue = minFilterValue; }
    public String getMaxFilterValue() { return maxFilterValue; }
    public void setMaxFilterValue(String maxFilterValue) { this.maxFilterValue = maxFilterValue; }
    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }
    public UserEntity getCreator() { return creator; }
    public void setCreator(UserEntity creator) { this.creator = creator; }
    public List<ChallengeVersionEntity> getChallengeVersions() { return challengeVersions; }
    public void setChallengeVersions(List<ChallengeVersionEntity> challengeVersions) { this.challengeVersions = challengeVersions; }

    public static class Builder {
        private long id;
        private String description;
        private GameModeEntity gameMode;
        private AttributeEntity filterAttribute;
        private String minFilterValue;
        private String maxFilterValue;
        private LocalDateTime creationDate;
        private UserEntity creator;
        private List<ChallengeVersionEntity> challengeVersions = new ArrayList<>();

        public Builder withId(long id) { this.id = id; return this; }
        public Builder withDescription(String description) { this.description = description; return this; }
        public Builder withGameMode(GameModeEntity gameMode) { this.gameMode = gameMode; return this; }
        public Builder withFilterAttribute(AttributeEntity filterAttribute) { this.filterAttribute = filterAttribute; return this; }
        public Builder withMinFilterValue(String minFilterValue) { this.minFilterValue = minFilterValue; return this; }
        public Builder withMaxFilterValue(String maxFilterValue) { this.maxFilterValue = maxFilterValue; return this; }
        public Builder withCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; return this; }
        public Builder withCreator(UserEntity creator) { this.creator = creator; return this; }
        public Builder withChallengeVersions(List<ChallengeVersionEntity> challengeVersions) { this.challengeVersions = challengeVersions; return this; }
        public ChallengeEntity build() { return new ChallengeEntity(this); }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChallengeEntity)) return false;
        ChallengeEntity that = (ChallengeEntity) o;
        return id == that.id &&
               Objects.equals(description, that.description) &&
               Objects.equals(gameMode, that.gameMode) &&
               Objects.equals(filterAttribute, that.filterAttribute) &&
               Objects.equals(minFilterValue, that.minFilterValue) &&
               Objects.equals(maxFilterValue, that.maxFilterValue) &&
               Objects.equals(creationDate, that.creationDate) &&
               Objects.equals(creator, that.creator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, gameMode, filterAttribute, minFilterValue, maxFilterValue, creationDate, creator);
    }

    @Override
    public String toString() {
        return "ChallengeEntity{" +
               "id=" + id +
               ", description='" + description + '\'' +
               ", gameMode=" + gameMode +
               ", filterAttribute=" + filterAttribute +
               ", minFilterValue='" + minFilterValue + '\'' +
               ", maxFilterValue='" + maxFilterValue + '\'' +
               ", creationDate=" + creationDate +
               ", creator=" + creator +
               '}';
    }
}
