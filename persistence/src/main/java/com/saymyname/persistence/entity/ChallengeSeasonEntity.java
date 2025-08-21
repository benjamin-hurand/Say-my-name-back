package com.saymyname.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "challenge_seasons", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "season_number", "start_date", "end_date" })
})
public class ChallengeSeasonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "season_number", nullable = false)
    private int seasonNumber;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    public ChallengeSeasonEntity() {
    }

    public ChallengeSeasonEntity(Long id, int seasonNumber, LocalDateTime startDate, LocalDateTime endDate) {
        this.id = id;
        this.seasonNumber = seasonNumber;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getSeasonNumber() {
        return seasonNumber;
    }

    public void setSeasonNumber(int seasonNumber) {
        this.seasonNumber = seasonNumber;
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

    // Builder
    public static class Builder {
        private Long id;
        private int seasonNumber;
        private LocalDateTime startDate;
        private LocalDateTime endDate;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withSeasonNumber(int seasonNumber) {
            this.seasonNumber = seasonNumber;
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

        public ChallengeSeasonEntity build() {
            return new ChallengeSeasonEntity(id, seasonNumber, startDate, endDate);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ChallengeSeasonEntity that = (ChallengeSeasonEntity) o;
        return id == that.id &&
                seasonNumber == that.seasonNumber &&
                Objects.equals(startDate, that.startDate) &&
                Objects.equals(endDate, that.endDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, seasonNumber, startDate, endDate);
    }

    @Override
    public String toString() {
        return "ChallengeSeasonEntity{" +
                "id=" + id +
                ", seasonNumber=" + seasonNumber +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
